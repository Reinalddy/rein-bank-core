package com.rein.bank.controller;

import com.rein.bank.dto.ApplyLoanRequest;
import com.rein.bank.entity.Loan;
import com.rein.bank.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/loans")
    public ResponseEntity<Loan> applyLoan(@Valid @RequestBody ApplyLoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.applyAndDisburseLoan(request));
    }

    @PostMapping("/loans/{loanNumber}/repay")
    public ResponseEntity<Loan> repayInstallment(@PathVariable String loanNumber) {
        return ResponseEntity.ok(loanService.repayInstallment(loanNumber));
    }

    @GetMapping("/accounts/{accountNumber}/loans")
    public ResponseEntity<List<Loan>> getAccountLoans(@PathVariable String accountNumber) {
        return ResponseEntity.ok(loanService.getLoansByAccount(accountNumber));
    }
}
