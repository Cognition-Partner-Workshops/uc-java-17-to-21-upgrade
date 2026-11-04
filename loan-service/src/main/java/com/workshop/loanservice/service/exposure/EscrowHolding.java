package com.workshop.loanservice.service.exposure;

import java.math.BigDecimal;

public final class EscrowHolding implements ExposureComponent {

    private final BigDecimal balance;

    public EscrowHolding(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() { return balance; }
}
