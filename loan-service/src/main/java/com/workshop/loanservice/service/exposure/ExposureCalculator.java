package com.workshop.loanservice.service.exposure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExposureCalculator {

    private ExposureCalculator() {
    }

    public static ExposureTotals summarize(List<ExposureComponent> components) {
        BigDecimal original = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        BigDecimal escrow = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal lateFees = BigDecimal.ZERO;
        int maxDaysPastDue = 0;
        List<PaymentRecord> payments = new ArrayList<>();

        for (ExposureComponent component : components) {
            switch (component) {
                case KnownExposureComponent known -> {
                    switch (known) {
                        case OutstandingPrincipal principal -> {
                            original = original.add(principal.getOriginalAmount());
                            outstanding = outstanding.add(principal.getCurrentBalance());
                        }
                        case EscrowHolding holding -> escrow = escrow.add(holding.getBalance());
                        case PaymentRecord payment -> {
                            paid = paid.add(payment.getTotalAmount());
                            lateFees = lateFees.add(payment.getLateFee());
                            payments.add(payment);
                        }
                        case DelinquencyNotice notice ->
                                maxDaysPastDue = Math.max(maxDaysPastDue, notice.getDaysPastDue());
                    }
                }
                default -> throw new IllegalArgumentException("Unknown exposure component: " + component.getClass().getName());
            }
        }

        LocalDate lastPaymentDate = null;
        if (!payments.isEmpty()) {
            payments.sort(Comparator.comparing(PaymentRecord::getPaymentDate));
            lastPaymentDate = payments.getLast().getPaymentDate();
        }
        return new ExposureTotals(original, outstanding, escrow, paid, lateFees, maxDaysPastDue, lastPaymentDate);
    }

    /**
     * HIGH: 90+ days past due. ELEVATED: 30+ days past due or any late fees. WATCH: credit score
     * below 680. Otherwise LOW.
     */
    public static String riskBand(ExposureTotals totals, Integer creditScore) {
        if (totals.getMaxDaysPastDue() >= 90) {
            return "HIGH";
        } else if (totals.getMaxDaysPastDue() >= 30 || totals.getTotalLateFees().signum() > 0) {
            return "ELEVATED";
        } else if (creditScore != null && creditScore < 680) {
            return "WATCH";
        }
        return "LOW";
    }
}
