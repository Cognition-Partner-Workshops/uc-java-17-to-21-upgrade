package com.workshop.loanservice.service.exposure;

public final class DelinquencyNotice implements ExposureComponent {

    private final int daysPastDue;

    public DelinquencyNotice(int daysPastDue) {
        this.daysPastDue = daysPastDue;
    }

    public int getDaysPastDue() { return daysPastDue; }
}
