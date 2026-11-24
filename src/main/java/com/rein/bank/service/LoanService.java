package com.rein.bank.service;

import com.rein.bank.dto.ApplyLoanRequest;
import com.rein.bank.entity.Loan;

import java.util.List;

public interface LoanService {

    Loan applyAndDisburseLoan(ApplyLoanRequest request);

    Loan repayInstallment(String loanNumber);

    List<Loan> getLoansByAccount(String accountNumber);
}
