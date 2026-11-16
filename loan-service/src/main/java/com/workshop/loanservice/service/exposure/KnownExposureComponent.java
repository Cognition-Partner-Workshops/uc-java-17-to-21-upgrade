package com.workshop.loanservice.service.exposure;

public sealed interface KnownExposureComponent extends ExposureComponent
        permits OutstandingPrincipal, EscrowHolding, PaymentRecord, DelinquencyNotice {
}
