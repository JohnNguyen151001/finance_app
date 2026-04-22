package com.financeapp.mobile.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.financeapp.mobile.R;

/**
 * Gauge bán nguyệt 180° — track nét đứt, fill gradient xanh→vàng→đỏ theo tỷ lệ chi tiêu.
 * Gọi {@link #setProgress(float)} (0..1) để cập nhật với animation.
 */
public class SemiCircleGaugeView extends View {

    private static final int ANIM_DURATION_MS = 900;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private float targetProgress;
    private float animatedProgress;
    private ValueAnimator animator;

    private int colorGreen;
    private int colorYellow;
    private int colorRed;

    public SemiCircleGaugeView(Context context) {
        super(context);
        init();
    }

    public SemiCircleGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SemiCircleGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorGreen = ContextCompat.getColor(getContext(), R.color.spend_green);
        colorYellow = 0xFFFFC107;
        colorRed = ContextCompat.getColor(getContext(), R.color.expense_red);

        float strokeW = dp(14);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.BUTT);
        trackPaint.setStrokeWidth(strokeW);
        trackPaint.setColor(0xFFEEEEEE);
        trackPaint.setPathEffect(new DashPathEffect(new float[]{dp(6), dp(5)}, 0));

        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStrokeWidth(strokeW);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(0xFFFFFFFF);
    }

    /** Animate gauge to new progress value (0..1). */
    public void setProgress(float p) {
        targetProgress = Math.max(0f, Math.min(1f, p));
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(animatedProgress, targetProgress);
        animator.setDuration(ANIM_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animatedProgress = (float) a.getAnimatedValue();
            rebuildGradient();
            invalidate();
        });
        animator.start();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int desired = w / 2 + (int) dp(24);
        int h = resolveSize(desired, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pad = dp(20);
        float stroke = dp(14);
        arcRect.set(pad + stroke / 2f, pad + stroke / 2f,
                w - pad - stroke / 2f, 2f * (h - pad) - stroke / 2f);
        rebuildGradient();
    }

    private void rebuildGradient() {
        if (arcRect.isEmpty()) return;
        float cx = arcRect.centerX();
        float r = arcRect.width() / 2f;
        // Gradient left→right maps to 0%→100% spend
        fillPaint.setShader(new LinearGradient(
                cx - r, 0, cx + r, 0,
                new int[]{colorGreen, colorYellow, colorRed},
                new float[]{0f, 0.65f, 1f},
                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Track (dashed full 180°)
        canvas.drawArc(arcRect, 180f, 180f, false, trackPaint);

        // Fill arc
        float sweep = 180f * animatedProgress;
        if (sweep > 0.5f) {
            canvas.drawArc(arcRect, 180f, sweep, false, fillPaint);
        }

        // Endpoint dot at tip of fill arc
        if (sweep > 2f) {
            double rad = Math.toRadians(180.0 + sweep);
            float cx = arcRect.centerX();
            float cy = arcRect.top + arcRect.height() / 2f;
            float rx = arcRect.width() / 2f;
            float ry = arcRect.height() / 2f;
            float dotX = (float) (cx + rx * Math.cos(rad));
            float dotY = (float) (cy + ry * Math.sin(rad));
            float dotR = dp(7);
            dotPaint.setColor(interpolateColor(animatedProgress));
            canvas.drawCircle(dotX, dotY, dotR, dotPaint);
            dotPaint.setColor(0xFFFFFFFF);
            canvas.drawCircle(dotX, dotY, dotR - dp(2.5f), dotPaint);
        }
    }

    private int interpolateColor(float t) {
        if (t <= 0.65f) {
            float f = t / 0.65f;
            return blendColor(colorGreen, colorYellow, f);
        } else {
            float f = (t - 0.65f) / 0.35f;
            return blendColor(colorYellow, colorRed, f);
        }
    }

    private static int blendColor(int c1, int c2, float t) {
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >> 8) & 0xFF) * (1 - t) + ((c2 >> 8) & 0xFF) * t);
        int b = (int) ((c1 & 0xFF) * (1 - t) + (c2 & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
