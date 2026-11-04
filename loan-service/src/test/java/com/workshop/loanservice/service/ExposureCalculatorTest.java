package com.workshop.loanservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.workshop.loanservice.service.exposure.DelinquencyNotice;
import com.workshop.loanservice.service.exposure.EscrowHolding;
import com.workshop.loanservice.service.exposure.ExposureCalculator;
import com.workshop.loanservice.service.exposure.ExposureComponent;
import com.workshop.loanservice.service.exposure.ExposureTotals;
import com.workshop.loanservice.service.exposure.OutstandingPrincipal;
import com.workshop.loanservice.service.exposure.PaymentRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExposureCalculatorTest {

    @Test
    void sumsComponentsAndPicksLatestPayment() {
        ExposureTotals totals = ExposureCalculator.summarize(List.of(
                new OutstandingPrincipal("LN-1", new BigDecimal("100"), new BigDecimal("80")),
                new EscrowHolding(new BigDecimal("5")),
                new PaymentRecord(LocalDate.of(2025, 12, 1), new BigDecimal("10"), BigDecimal.ZERO),
                new PaymentRecord(LocalDate.of(2025, 11, 1), new BigDecimal("10"), new BigDecimal("2.50")),
                new DelinquencyNotice(15)));

        assertEquals(new BigDecimal("100"), totals.getTotalOriginalAmount());
        assertEquals(new BigDecimal("80"), totals.getTotalOutstandingBalance());
        assertEquals(new BigDecimal("5"), totals.getTotalEscrowBalance());
        assertEquals(new BigDecimal("20"), totals.getTotalPaid());
        assertEquals(new BigDecimal("2.50"), totals.getTotalLateFees());
        assertEquals(15, totals.getMaxDaysPastDue());
        assertEquals(LocalDate.of(2025, 12, 1), totals.getLastPaymentDate());
    }

    @Test
    void emptyExposureHasNoLastPayment() {
        ExposureTotals totals = ExposureCalculator.summarize(List.of());
        assertNull(totals.getLastPaymentDate());
        assertEquals("LOW", ExposureCalculator.riskBand(totals, null));
    }

    @Test
    void riskBandsFollowDelinquencyLateFeesAndCreditScore() {
        assertEquals("HIGH", band(new DelinquencyNotice(90)));
        assertEquals("ELEVATED", band(new DelinquencyNotice(30)));
        assertEquals("ELEVATED",
                band(new PaymentRecord(LocalDate.of(2025, 1, 1), BigDecimal.ONE, BigDecimal.ONE)));
        assertEquals("WATCH", ExposureCalculator.riskBand(ExposureCalculator.summarize(List.of()), 679));
        assertEquals("LOW", ExposureCalculator.riskBand(ExposureCalculator.summarize(List.of()), 680));
    }

    @Test
    void rejectsUnknownComponents() {
        assertThrows(IllegalArgumentException.class,
                () -> ExposureCalculator.summarize(List.of(new ExposureComponent() { })));
    }

    private static String band(ExposureComponent component) {
        return ExposureCalculator.riskBand(ExposureCalculator.summarize(List.of(component)), 800);
    }
}
