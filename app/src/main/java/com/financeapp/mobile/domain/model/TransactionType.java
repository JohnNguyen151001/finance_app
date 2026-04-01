package com.financeapp.mobile.domain.model;

/**
 * Loại giao dịch — đồng bộ với cột {@code transactions.type} trong Room.
 */
public enum TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
    LEND,
    BORROW
}
