package com.financeapp.mobile.ui.report;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.domain.model.TransactionType;
import com.financeapp.mobile.ui.chart.ChartPoint;
import com.financeapp.mobile.ui.format.MoneyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<ReportUiModel> ui = new MutableLiveData<>();

    public ReportViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        categoryRepository = new CategoryRepository(application);
        refresh();
    }

    public LiveData<ReportUiModel> getUi() {
        return ui;
    }

    public void refresh() {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            String uid = u != null ? u.getUid() : "";
            long foodCatId = -1;
            for (CategoryEntity c : categoryRepository.getByKind(uid, "EXPENSE")) {
                if ("Ăn uống".equals(c.name)) {
                    foodCatId = c.id;
                    break;
                }
            }
            String title = "Ăn uống";

            Calendar monthStart = Calendar.getInstance();
            monthStart.set(Calendar.DAY_OF_MONTH, 1);
            monthStart.set(Calendar.HOUR_OF_DAY, 0);
            monthStart.set(Calendar.MINUTE, 0);
            monthStart.set(Calendar.SECOND, 0);
            monthStart.set(Calendar.MILLISECOND, 0);
            long from = monthStart.getTimeInMillis();
            Calendar next = (Calendar) monthStart.clone();
            next.add(Calendar.MONTH, 1);
            long to = next.getTimeInMillis();

            int dim = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
            double[] daily = new double[dim + 1];
            double sum = 0;
            if (foodCatId >= 0) {
                List<TransactionEntity> list = transactionRepository.getBetween(uid, from, to);
                Calendar cal = Calendar.getInstance();
                for (TransactionEntity t : list) {
                    if (t.categoryId != foodCatId) {
                        continue;
                    }
                    if (!TransactionType.EXPENSE.name().equals(t.type)) {
                        continue;
                    }
                    sum += t.amount;
                    cal.setTimeInMillis(t.occurredAt);
                    int dom = cal.get(Calendar.DAY_OF_MONTH);
                    if (dom >= 1 && dom <= dim) {
                        daily[dom] += t.amount;
                    }
                }
            }

            Calendar today = Calendar.getInstance();
            int lastDay = dim;
            if (monthStart.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && monthStart.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                lastDay = Math.min(dim, today.get(Calendar.DAY_OF_MONTH));
            }

            List<ChartPoint> bar = new ArrayList<>();
            for (int d = 1; d <= lastDay; d++) {
                bar.add(new ChartPoint(d, (float) daily[d]));
            }

            List<ChartPoint> line = new ArrayList<>();
            double cum = 0;
            for (int d = 1; d <= lastDay; d++) {
                cum += daily[d];
                line.add(new ChartPoint(d, (float) cum));
            }

            ui.postValue(new ReportUiModel(
                    title,
                    MoneyUtils.formatSignedExpense(sum),
                    line,
                    bar
            ));
        });
    }
}
