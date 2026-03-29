package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VerificationCodeTest {

    @Test
    public void testGenerateCode_consistency() {
        String location = "Auditorium";
        long time = 1715850000000L; // Example timestamp

        int code1 = VerificationCodeFragment.generateCode(location, time);
        int code2 = VerificationCodeFragment.generateCode(location, time);

        assertEquals("Code should be consistent for the same input", code1, code2);
    }

    @Test
    public void testGenerateCode_differentLocation() {
        String loc1 = "Auditorium";
        String loc2 = "MetaForum";
        long time = 1715850000000L;

        int code1 = VerificationCodeFragment.generateCode(loc1, time);
        int code2 = VerificationCodeFragment.generateCode(loc2, time);

        assertTrue("Codes should differ for different locations", code1 != code2);
    }

    @Test
    public void testGenerateCode_differentTime() {
        String location = "Auditorium";
        long time1 = 1715850000000L;
        long time2 = 1715853600000L;

        int code1 = VerificationCodeFragment.generateCode(location, time1);
        int code2 = VerificationCodeFragment.generateCode(location, time2);

        assertTrue("Codes should differ for different times", code1 != code2);
    }

    @Test
    public void testGenerateCode_positive() {
        // Test with values that might produce a negative hash/product
        int code = VerificationCodeFragment.generateCode("Some very long location name that might cause overflow", -123456789L);
        assertTrue("Generated code should always be non-negative", code >= 0);
        assertTrue("Generated code should be within 6 digits", code < 1000000);
    }

    @Test
    public void testGenerateCode_nullLocation() {
        long time = 1715850000000L;
        int code = VerificationCodeFragment.generateCode(null, time);
        assertTrue("Should handle null location gracefully", code >= 0);
    }
}
