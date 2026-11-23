package com.rein.bank.repository;

import com.rein.bank.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByAccount_AccountNumberOrderByCreatedAtDesc(String accountNumber);
}