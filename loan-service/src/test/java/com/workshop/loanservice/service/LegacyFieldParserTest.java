package com.workshop.loanservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LegacyFieldParserTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "285,000|285000",
            "1,487.02|1487.02",
            "1,500,000|1500000",
            "0.00|0.00"
    })
    void parsesAmountsWithThousandsSeparators(String legacy, String expected) {
        assertEquals(new BigDecimal(expected), LegacyFieldParser.parseAmount(legacy));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankAmountIsZero(String legacy) {
        assertEquals(BigDecimal.ZERO, LegacyFieldParser.parseAmount(legacy));
    }

    @Test
    void malformedAmountIsRejected() {
        assertThrows(NumberFormatException.class, () -> LegacyFieldParser.parseAmount("12O,000"));
    }

    @Test
    void decimalIsTrimmedAndBlankIsZero() {
        assertEquals(new BigDecimal("4.750"), LegacyFieldParser.parseDecimal(" 4.750 "));
        assertEquals(BigDecimal.ZERO, LegacyFieldParser.parseDecimal(""));
    }

    @Test
    void integerIsTrimmedAndBlankIsNull() {
        assertEquals(745, LegacyFieldParser.parseInteger(" 745 "));
        assertNull(LegacyFieldParser.parseInteger(" "));
    }

    @ParameterizedTest
    @CsvSource({"ACT,Active", "CLO,Closed", "DFT,Default", "FRB,Forbearance", "XYZ,XYZ"})
    void expandsLoanStatusCodes(String code, String expected) {
        assertEquals(expected, LegacyFieldParser.expandStatusCode(code));
    }

    @Test
    void nullCodesExpandToUnknown() {
        assertEquals("Unknown", LegacyFieldParser.expandStatusCode(null));
        assertEquals("Unknown", LegacyFieldParser.expandPropertyType(null));
        assertEquals("Unknown", LegacyFieldParser.expandPaymentType(null));
        assertEquals("Unknown", LegacyFieldParser.expandPaymentStatus(null));
    }

    @Test
    void expandsPropertyPaymentTypeAndPaymentStatusCodes() {
        assertEquals("Condominium", LegacyFieldParser.expandPropertyType("CND"));
        assertEquals("Townhouse", LegacyFieldParser.expandPropertyType("TWN"));
        assertEquals("Multi-Family Residence", LegacyFieldParser.expandPropertyType("MFR"));
        assertEquals("Extra", LegacyFieldParser.expandPaymentType("EXT"));
        assertEquals("Partial", LegacyFieldParser.expandPaymentType("PRT"));
        assertEquals("Prepayment", LegacyFieldParser.expandPaymentType("PRE"));
        assertEquals("Reversed", LegacyFieldParser.expandPaymentStatus("REV"));
        assertEquals("Non-Sufficient Funds", LegacyFieldParser.expandPaymentStatus("NSF"));
        assertEquals("Pending", LegacyFieldParser.expandPaymentStatus("PND"));
        assertEquals("ZZZ", LegacyFieldParser.expandPropertyType("ZZZ"));
        assertEquals("ZZZ", LegacyFieldParser.expandPaymentType("ZZZ"));
        assertEquals("ZZZ", LegacyFieldParser.expandPaymentStatus("ZZZ"));
    }

    @Test
    void parsesLegacyDates() {
        assertEquals(LocalDate.of(2025, 12, 15), LegacyFieldParser.parseDate("12/15/2025"));
        assertEquals(LocalDate.of(2024, 2, 29), LegacyFieldParser.parseDate(" 02/29/2024 "));
        assertNull(LegacyFieldParser.parseDate(""));
        assertNull(LegacyFieldParser.parseDate(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-12-15", "13/01/2025", "02/30/2025", "1/5/2025"})
    void rejectsMalformedOrImpossibleLegacyDates(String legacy) {
        assertThrows(DateTimeParseException.class, () -> LegacyFieldParser.parseDate(legacy));
    }
}
