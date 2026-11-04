package com.workshop.loanservice.dto;

import java.math.BigDecimal;

/**
 * Aggregated exposure for one borrower across all of their loans and payments.
 */
public class BorrowerExposureDto {

    private String borrowerId;
    private String borrowerName;
    private Integer creditScore;
    private int loanCount;
    private BigDecimal totalOriginalAmount;
    private BigDecimal totalOutstandingBalance;
    private BigDecimal totalEscrowBalance;
    private BigDecimal totalPaid;
    private BigDecimal totalLateFees;
    private int maxDaysPastDue;
    private String lastPaymentDate;
    private String riskBand;
    private String outstandingBalanceDisplay;
    private String selfLink;

    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public int getLoanCount() { return loanCount; }
    public void setLoanCount(int loanCount) { this.loanCount = loanCount; }
    public BigDecimal getTotalOriginalAmount() { return totalOriginalAmount; }
    public void setTotalOriginalAmount(BigDecimal totalOriginalAmount) { this.totalOriginalAmount = totalOriginalAmount; }
    public BigDecimal getTotalOutstandingBalance() { return totalOutstandingBalance; }
    public void setTotalOutstandingBalance(BigDecimal totalOutstandingBalance) { this.totalOutstandingBalance = totalOutstandingBalance; }
    public BigDecimal getTotalEscrowBalance() { return totalEscrowBalance; }
    public void setTotalEscrowBalance(BigDecimal totalEscrowBalance) { this.totalEscrowBalance = totalEscrowBalance; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
    public BigDecimal getTotalLateFees() { return totalLateFees; }
    public void setTotalLateFees(BigDecimal totalLateFees) { this.totalLateFees = totalLateFees; }
    public int getMaxDaysPastDue() { return maxDaysPastDue; }
    public void setMaxDaysPastDue(int maxDaysPastDue) { this.maxDaysPastDue = maxDaysPastDue; }
    public String getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(String lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
    public String getRiskBand() { return riskBand; }
    public void setRiskBand(String riskBand) { this.riskBand = riskBand; }
    public String getOutstandingBalanceDisplay() { return outstandingBalanceDisplay; }
    public void setOutstandingBalanceDisplay(String outstandingBalanceDisplay) { this.outstandingBalanceDisplay = outstandingBalanceDisplay; }
    public String getSelfLink() { return selfLink; }
    public void setSelfLink(String selfLink) { this.selfLink = selfLink; }
}
