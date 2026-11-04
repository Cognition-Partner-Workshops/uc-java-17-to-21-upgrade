package com.workshop.loanservice.service.exposure;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class PaymentRecord implements ExposureComponent {

    private final LocalDate paymentDate;
    private final BigDecimal totalAmount;
    private final BigDecimal lateFee;

    public PaymentRecord(LocalDate paymentDate, BigDecimal totalAmount, BigDecimal lateFee) {
        this.paymentDate = paymentDate;
        this.totalAmount = totalAmount;
        this.lateFee = lateFee;
    }

    public LocalDate getPaymentDate() { return paymentDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getLateFee() { return lateFee; }
}
