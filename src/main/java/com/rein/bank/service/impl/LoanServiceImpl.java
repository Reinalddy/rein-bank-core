package com.rein.bank.service.impl;

import com.rein.bank.dto.ApplyLoanRequest;
import com.rein.bank.entity.Account;
import com.rein.bank.entity.LedgerEntry;
import com.rein.bank.entity.Loan;
import com.rein.bank.repository.AccountRepository;
import com.rein.bank.repository.LedgerEntryRepository;
import com.rein.bank.repository.LoanRepository;
import com.rein.bank.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.0150"); // 1.5% per bulan

    @Override
    @Transactional
    public Loan applyAndDisburseLoan(ApplyLoanRequest request) {
        Account account = accountRepository.findByAccountNumberForUpdate(request.accountNumber())
                .orElseThrow(
                        () -> new IllegalArgumentException("Rekening " + request.accountNumber() + " tidak ditemukan"));

        BigDecimal principal = request.principalAmount();
        BigDecimal months = BigDecimal.valueOf(request.tenorMonths());

        // Hitung total bunga & total kewajiban bayar
        BigDecimal totalInterest = principal.multiply(MONTHLY_INTEREST_RATE).multiply(months);
        BigDecimal totalPayable = principal.add(totalInterest).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = totalPayable.divide(months, 2, RoundingMode.HALF_UP);

        String loanNumber = generateUniqueLoanNumber();

        Loan loan = Loan.builder()
                .loanNumber(loanNumber)
                .account(account)
                .principalAmount(principal)
                .monthlyInterestRate(MONTHLY_INTEREST_RATE)
                .tenorMonths(request.tenorMonths())
                .totalPayable(totalPayable)
                .monthlyInstallment(monthlyInstallment)
                .remainingBalance(totalPayable)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        // Cairkan pokok pinjaman langsung ke saldo rekening nasabah
        account.setBalance(account.getBalance().add(principal));
        accountRepository.save(account);

        // Catat pencairan ke buku besar (Ledger)
        LedgerEntry disbursementEntry = LedgerEntry.builder()
                .referenceId("LOAN-DISB-" + loanNumber)
                .account(account)
                .entryType("CREDIT")
                .amount(principal)
                .description("Pencairan pinjaman " + loanNumber + " (" + request.tenorMonths() + " bulan)")
                .build();
        ledgerEntryRepository.save(disbursementEntry);

        return savedLoan;
    }

    @Override
    @Transactional
    public Loan repayInstallment(String loanNumber) {
        Loan loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new IllegalArgumentException("Pinjaman " + loanNumber + " tidak ditemukan"));

        if ("PAID_OFF".equals(loan.getStatus())) {
            throw new IllegalArgumentException("Pinjaman " + loanNumber + " sudah lunas");
        }

        Account account = accountRepository.findByAccountNumberForUpdate(loan.getAccount().getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Rekening nasabah tidak ditemukan"));

        // Untuk cicilan terakhir, bayar sisa tagihan exact agar tidak selisih
        // pembulatan koma
        int nextInstallmentNumber = loan.getPaidInstallments() + 1;
        BigDecimal paymentAmount = (nextInstallmentNumber == loan.getTenorMonths())
                ? loan.getRemainingBalance()
                : loan.getMonthlyInstallment();

        if (account.getBalance().compareTo(paymentAmount) < 0) {
            throw new IllegalArgumentException(
                    "Saldo rekening tidak mencukupi untuk membayar cicilan sebesar " + paymentAmount);
        }

        // Potong saldo rekening
        account.setBalance(account.getBalance().subtract(paymentAmount));
        accountRepository.save(account);

        // Update status & sisa tagihan pinjaman
        loan.setPaidInstallments(nextInstallmentNumber);
        loan.setRemainingBalance(loan.getRemainingBalance().subtract(paymentAmount));

        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0
                || nextInstallmentNumber >= loan.getTenorMonths()) {
            loan.setRemainingBalance(BigDecimal.ZERO);
            loan.setStatus("PAID_OFF");
        }

        Loan updatedLoan = loanRepository.save(loan);

        // Catat pembayaran cicilan ke Ledger
        LedgerEntry repaymentEntry = LedgerEntry.builder()
                .referenceId("LOAN-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .account(account)
                .entryType("DEBIT")
                .amount(paymentAmount)
                .description("Pembayaran cicilan ke-" + nextInstallmentNumber + " pinjaman " + loanNumber)
                .build();
        ledgerEntryRepository.save(repaymentEntry);

        return updatedLoan;
    }

    @Override
    public List<Loan> getLoansByAccount(String accountNumber) {
        return loanRepository.findByAccount_AccountNumberOrderByCreatedAtDesc(accountNumber);
    }

    private String generateUniqueLoanNumber() {
        String loanNum;
        do {
            long randomDigits = ThreadLocalRandom.current().nextLong(10000000L, 99999999L);
            loanNum = "LN-" + randomDigits;
        } while (loanRepository.findByLoanNumber(loanNum).isPresent());
        return loanNum;
    }
}
