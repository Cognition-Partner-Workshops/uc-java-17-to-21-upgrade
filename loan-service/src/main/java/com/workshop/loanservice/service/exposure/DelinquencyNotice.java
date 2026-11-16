package com.workshop.loanservice.service.exposure;

public record DelinquencyNotice(int daysPastDue) implements KnownExposureComponent {

    public int getDaysPastDue() { return daysPastDue; }
}
