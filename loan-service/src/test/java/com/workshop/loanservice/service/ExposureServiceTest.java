package com.workshop.loanservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.workshop.loanservice.dto.BorrowerExposureDto;
import com.workshop.loanservice.entity.LegacyBorrower;
import com.workshop.loanservice.entity.LegacyLoanAccount;
import com.workshop.loanservice.entity.LegacyPayment;
import com.workshop.loanservice.repository.LegacyBorrowerRepository;
import com.workshop.loanservice.repository.LegacyLoanAccountRepository;
import com.workshop.loanservice.repository.LegacyPaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExposureServiceTest {

    private LegacyBorrowerRepository borrowerRepository;
    private LegacyLoanAccountRepository loanAccountRepository;
    private LegacyPaymentRepository paymentRepository;
    private ExposureService service;

    @BeforeEach
    void setUp() {
        borrowerRepository = mock(LegacyBorrowerRepository.class);
        loanAccountRepository = mock(LegacyLoanAccountRepository.class);
        paymentRepository = mock(LegacyPaymentRepository.class);
        service = new ExposureService(borrowerRepository, loanAccountRepository, paymentRepository,
                2, 0, "https://loans.example.com");
    }

    @AfterEach
    void tearDown() {
        service.destroy();
    }

    @Test
    void aggregatesLoansAndPaymentsAcrossTheFanOut() {
        when(borrowerRepository.findById("B-1")).thenReturn(Optional.of(borrower("B-1", "620")));
        when(loanAccountRepository.findByBorrowerId("B-1"))
                .thenReturn(List.of(loan("LN-A", "100,000", "90,000.50", "1,000.00", "0"),
                        loan("LN-B", "50,000", "10,000", "", "95")));
        when(paymentRepository.findByLoanAccountNumber("LN-A"))
                .thenReturn(List.of(payment("01/15/2025", "1,200.00", "0.00")));
        when(paymentRepository.findByLoanAccountNumber("LN-B"))
                .thenReturn(List.of(payment("03/01/2025", "800.00", "25.00")));

        BorrowerExposureDto dto = service.getExposure("B-1");

        assertEquals("Jo Bloggs", dto.getBorrowerName());
        assertEquals(2, dto.getLoanCount());
        assertEquals(new BigDecimal("150000"), dto.getTotalOriginalAmount());
        assertEquals(new BigDecimal("100000.50"), dto.getTotalOutstandingBalance());
        assertEquals(new BigDecimal("2000.00"), dto.getTotalPaid());
        assertEquals(95, dto.getMaxDaysPastDue());
        assertEquals("2025-03-01", dto.getLastPaymentDate());
        assertEquals("HIGH", dto.getRiskBand());
        assertEquals("£100,000.50", dto.getOutstandingBalanceDisplay());
        assertEquals("https://loans.example.com/api/borrowers/B-1/exposure", dto.getSelfLink());
    }

    @Test
    void borrowerWithoutLoansHasEmptyExposure() {
        when(borrowerRepository.findById("B-2")).thenReturn(Optional.of(borrower("B-2", "")));
        when(loanAccountRepository.findByBorrowerId("B-2")).thenReturn(List.of());

        BorrowerExposureDto dto = service.getExposure("B-2");

        assertEquals(0, dto.getLoanCount());
        assertNull(dto.getCreditScore());
        assertNull(dto.getLastPaymentDate());
        assertEquals("LOW", dto.getRiskBand());
    }

    @Test
    void unknownBorrowerFails() {
        when(borrowerRepository.findById("B-404")).thenReturn(Optional.empty());
        when(loanAccountRepository.findByBorrowerId("B-404")).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.getExposure("B-404"));
    }

    private static LegacyBorrower borrower(String id, String creditScore) {
        LegacyBorrower borrower = new LegacyBorrower();
        borrower.setBorrowerId(id);
        borrower.setFirstName("Jo");
        borrower.setLastName("Bloggs");
        borrower.setCreditScore(creditScore);
        return borrower;
    }

    private static LegacyLoanAccount loan(String number, String original, String balance, String escrow,
                                          String daysPastDue) {
        LegacyLoanAccount loan = new LegacyLoanAccount();
        loan.setLoanAccountNumber(number);
        loan.setOriginalAmount(original);
        loan.setCurrentBalance(balance);
        loan.setEscrowBalance(escrow);
        loan.setDelinquencyDays(daysPastDue);
        return loan;
    }

    private static LegacyPayment payment(String date, String total, String lateFee) {
        LegacyPayment payment = new LegacyPayment();
        payment.setPaymentDate(date);
        payment.setTotalAmount(total);
        payment.setLateFee(lateFee);
        return payment;
    }
}
