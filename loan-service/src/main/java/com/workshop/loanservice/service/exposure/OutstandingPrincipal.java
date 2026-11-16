package com.workshop.loanservice.service.exposure;

import java.math.BigDecimal;

public record OutstandingPrincipal(String loanAccountNumber, BigDecimal originalAmount,
                                   BigDecimal currentBalance) implements KnownExposureComponent {

    public String getLoanAccountNumber() { return loanAccountNumber; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
}
