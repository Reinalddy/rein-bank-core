package com.rein.bank.service;

import com.rein.bank.dto.CreateAccountRequest;
import com.rein.bank.dto.TransferRequest;
import com.rein.bank.dto.TransferResponse;
import com.rein.bank.entity.Account;
import com.rein.bank.entity.LedgerEntry;

import java.util.List;

public interface BankService {
        Account createAccount(CreateAccountRequest request);

        Account getAccount(String accountNumber);

        List<LedgerEntry> getAccountMutations(String accountNumber);

        TransferResponse transfer(TransferRequest request);
}
