package com.rein.bank.repository;

import com.rein.bank.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    List<Loan> findByAccount_AccountNumberOrderByCreatedAtDesc(String accountNumber);
}
