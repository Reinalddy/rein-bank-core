package com.rein.bank.controller;

import com.rein.bank.dto.CreateAccountRequest;
import com.rein.bank.dto.TransferRequest;
import com.rein.bank.dto.TransferResponse;
import com.rein.bank.entity.Account;
import com.rein.bank.entity.LedgerEntry;
import com.rein.bank.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankService.createAccount(request));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankService.getAccount(accountNumber));
    }

    @GetMapping("/accounts/{accountNumber}/mutations")
    public ResponseEntity<List<LedgerEntry>> getMutations(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankService.getAccountMutations(accountNumber));
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(bankService.transfer(request));
    }
}