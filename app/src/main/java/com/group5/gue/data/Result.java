package com.group5.gue.data;

/**
 * A class wrapper that holds a result: either Success with data or an Error with an exception.
 *
 * @param <T> The type of the data held by a Successful result.
 */
public class Result<T> {
    
    /**
     * Private constructor to prevent direct instantiation of the base Result class.
     * This ensures that only Success and Error instances can exist.
     */
    private Result() {
    }

    /**
     * Returns a string representation of the Result, indicating whether it is
     * a success or an error, along with the corresponding data or exception.
     * 
     * @return Formatted string containing result details.
     */
    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Result.Success success = (Result.Success) this;
            // Format success message with data string
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Result.Error error = (Result.Error) this;
            // Format error message with exception string
            return "Error[exception=" + error.getError().toString() + "]";
        }
        return "";
    }

    /**
     * Subclass representing a successful operation.
     * 
     * @param <T> The type of the payload data.
     */
    public final static class Success<T> extends Result<T> {
        private T data;

        /**
         * Constructs a Success result with the provided data.
         * 
         * @param data The data object to be wrapped.
         */
        public Success(T data) {
            this.data = data;
        }

        /**
         * Getter for the success payload.
         * 
         * @return The data object.
         */
        public T getData() {
            return this.data;
        }
    }

    /**
     * Sub-class representing a failed operation.
     * 
     * @param <T> The type parameter (not used but required for class signature).
     */
    public final static class Error<T> extends Result<T> {
        private Exception error;

        /**
         * Constructs an Error result with the provided exception.
         * 
         * @param error The exception that caused the failure.
         */
        public Error(Exception error) {
            this.error = error;
        }

        /**
         * Getter for the error exception.
         * 
         * @return The stored exception.
         */
        public Exception getError() {
            return this.error;
        }
    }
}
