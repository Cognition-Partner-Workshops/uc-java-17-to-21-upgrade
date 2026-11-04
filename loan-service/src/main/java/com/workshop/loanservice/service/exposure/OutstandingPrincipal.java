package com.workshop.loanservice.service.exposure;

import java.math.BigDecimal;

public final class OutstandingPrincipal implements ExposureComponent {

    private final String loanAccountNumber;
    private final BigDecimal originalAmount;
    private final BigDecimal currentBalance;

    public OutstandingPrincipal(String loanAccountNumber, BigDecimal originalAmount, BigDecimal currentBalance) {
        this.loanAccountNumber = loanAccountNumber;
        this.originalAmount = originalAmount;
        this.currentBalance = currentBalance;
    }

    public String getLoanAccountNumber() { return loanAccountNumber; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
}
