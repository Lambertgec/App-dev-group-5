package com.group5.gue.data;

import android.util.Log;

/**
 * A generic class that holds a result success w/ data or an error exception.
 */
public class Result<T> {
    private static final String LOG_TAG = "Result";
    // hide the private constructor to limit subclass types (Success, Error)
    private Result() {
    }

    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Result.Success success = (Result.Success) this;
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Result.Error error = (Result.Error) this;
            return "Error[exception=" + error.getError().toString() + "]";
        }
        return "";
    }

    // Success sub-class
    public final static class Success<T> extends Result<T> {
        private T data;

        public Success(T data) {
            this.data = data;
            Log.d(LOG_TAG, "Success: " + String.valueOf(data));
        }

        public T getData() {
            return this.data;
        }
    }

    // Error sub-class
    public final static class Error<T> extends Result<T> {
        private Exception error;

        public Error(Exception error) {
            this.error = error;
            Log.e(LOG_TAG, "Error", error);
        }

        public Exception getError() {
            return this.error;
        }
    }
}