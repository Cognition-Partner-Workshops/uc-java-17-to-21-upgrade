package com.workshop.loanservice.service.exposure;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExposureTotals {

    private final BigDecimal totalOriginalAmount;
    private final BigDecimal totalOutstandingBalance;
    private final BigDecimal totalEscrowBalance;
    private final BigDecimal totalPaid;
    private final BigDecimal totalLateFees;
    private final int maxDaysPastDue;
    private final LocalDate lastPaymentDate;

    public ExposureTotals(BigDecimal totalOriginalAmount, BigDecimal totalOutstandingBalance,
                          BigDecimal totalEscrowBalance, BigDecimal totalPaid, BigDecimal totalLateFees,
                          int maxDaysPastDue, LocalDate lastPaymentDate) {
        this.totalOriginalAmount = totalOriginalAmount;
        this.totalOutstandingBalance = totalOutstandingBalance;
        this.totalEscrowBalance = totalEscrowBalance;
        this.totalPaid = totalPaid;
        this.totalLateFees = totalLateFees;
        this.maxDaysPastDue = maxDaysPastDue;
        this.lastPaymentDate = lastPaymentDate;
    }

    public BigDecimal getTotalOriginalAmount() { return totalOriginalAmount; }
    public BigDecimal getTotalOutstandingBalance() { return totalOutstandingBalance; }
    public BigDecimal getTotalEscrowBalance() { return totalEscrowBalance; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public BigDecimal getTotalLateFees() { return totalLateFees; }
    public int getMaxDaysPastDue() { return maxDaysPastDue; }
    public LocalDate getLastPaymentDate() { return lastPaymentDate; }
}
