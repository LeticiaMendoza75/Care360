package com.silveira.care360.domain.common;

public interface ResultCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
