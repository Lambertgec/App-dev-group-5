package com.group5.gue.data;

import org.junit.Test;
import static org.junit.Assert.*;

public class ResultTest {

    @Test
    public void success_toString() {
        Result.Success<String> success = new Result.Success<>("Test Data");
        assertEquals("Success[data=Test Data]", success.toString());
    }

    @Test
    public void error_toString() {
        Exception exception = new Exception("Test Exception");
        Result.Error<String> error = new Result.Error<>(exception);
        assertEquals("Error[exception=java.lang.Exception: Test Exception]", error.toString());
    }

    @Test
    public void success_getData() {
        String data = "Some data";
        Result.Success<String> success = new Result.Success<>(data);
        assertEquals(data, success.getData());
    }

    @Test
    public void error_getError() {
        Exception exception = new Exception("Failure");
        Result.Error<String> error = new Result.Error<>(exception);
        assertEquals(exception, error.getError());
    }
}
