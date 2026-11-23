package com.rein.bank.service;

import com.rein.bank.dto.CreateAccountRequest;
import com.rein.bank.dto.TransferRequest;
import com.rein.bank.dto.TransferResponse;
import com.rein.bank.entity.Account;
import com.rein.bank.entity.LedgerEntry;
import com.rein.bank.repository.AccountRepository;
import com.rein.bank.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BankService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        String generatedAccNum = generateUniqueAccountNumber();
        BigDecimal initialBalance = request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(generatedAccNum)
                .ownerName(request.ownerName())
                .balance(initialBalance)
                .build();

        Account savedAccount = accountRepository.save(account);

        // Jika ada setoran awal > 0, catat ke ledger sebagai CREDIT pembukaan rekening
        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            LedgerEntry initialEntry = LedgerEntry.builder()
                    .referenceId("INIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .account(savedAccount)
                    .entryType("CREDIT")
                    .amount(initialBalance)
                    .description("Setoran awal pembukaan rekening")
                    .build();
            ledgerEntryRepository.save(initialEntry);
        }

        return savedAccount;
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Rekening " + accountNumber + " tidak ditemukan"));
    }

    public List<LedgerEntry> getAccountMutations(String accountNumber) {
        getAccount(accountNumber);
        return ledgerEntryRepository.findByAccount_AccountNumberOrderByCreatedAtDesc(accountNumber);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.fromAccountNumber().equals(request.toAccountNumber())) {
            throw new IllegalArgumentException("Tidak bisa transfer ke rekening sendiri");
        }

        // Urutkan lock berdasarkan nomor rekening untuk mencegah database deadlock
        String firstLockAcc = request.fromAccountNumber().compareTo(request.toAccountNumber()) < 0
                ? request.fromAccountNumber() : request.toAccountNumber();
        String secondLockAcc = request.fromAccountNumber().compareTo(request.toAccountNumber()) < 0
                ? request.toAccountNumber() : request.fromAccountNumber();

        Account firstLocked = accountRepository.findByAccountNumberForUpdate(firstLockAcc)
                .orElseThrow(() -> new IllegalArgumentException("Rekening " + firstLockAcc + " tidak ditemukan"));
        Account secondLocked = accountRepository.findByAccountNumberForUpdate(secondLockAcc)
                .orElseThrow(() -> new IllegalArgumentException("Rekening " + secondLockAcc + " tidak ditemukan"));

        Account sender = firstLocked.getAccountNumber().equals(request.fromAccountNumber()) ? firstLocked : secondLocked;
        Account receiver = firstLocked.getAccountNumber().equals(request.toAccountNumber()) ? firstLocked : secondLocked;

        // Validasi saldo pengirim
        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk transfer");
        }

        // Update saldo kedua rekening
        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Buat Reference ID tunggal untuk pasangan Double-Entry Ledger
        String refId = "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String desc = (request.description() != null && !request.description().isBlank())
                ? request.description() : "Transfer dana";

        // 1. Catat DEBIT pada pengirim
        LedgerEntry debitEntry = LedgerEntry.builder()
                .referenceId(refId)
                .account(sender)
                .entryType("DEBIT")
                .amount(request.amount())
                .description(desc + " ke " + receiver.getAccountNumber())
                .build();

        // 2. Catat CREDIT pada penerima
        LedgerEntry creditEntry = LedgerEntry.builder()
                .referenceId(refId)
                .account(receiver)
                .entryType("CREDIT")
                .amount(request.amount())
                .description(desc + " dari " + sender.getAccountNumber())
                .build();

        ledgerEntryRepository.saveAll(List.of(debitEntry, creditEntry));

        return new TransferResponse(
                refId,
                sender.getAccountNumber(),
                receiver.getAccountNumber(),
                request.amount(),
                sender.getBalance(),
                "SUCCESS",
                LocalDateTime.now()
        );
    }

    private String generateUniqueAccountNumber() {
        String accNum;
        do {
            long randomDigits = ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L);
            accNum = String.valueOf(randomDigits);
        } while (accountRepository.findByAccountNumber(accNum).isPresent());
        return accNum;
    }
}