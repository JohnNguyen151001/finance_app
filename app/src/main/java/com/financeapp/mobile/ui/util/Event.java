package com.financeapp.mobile.ui.util;

import androidx.annotation.Nullable;

/**
 * One-shot payload cho LiveData (ví dụ toast sau copy) — mỗi lần post instance mới,
 * observer gọi {@link #getContentIfNotHandled()} để tiêu thụ một lần.
 */
public final class Event<T> {

    private final T content;
    private boolean handled;

    public Event(T content) {
        this.content = content;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
