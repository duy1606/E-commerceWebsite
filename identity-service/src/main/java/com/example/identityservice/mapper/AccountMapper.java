package com.example.identityservice.mapper;

import com.example.identityservice.dto.request.AccountCreateRequest;
import com.example.identityservice.dto.response.AccountResponse;
import com.example.identityservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Account toAccount(AccountCreateRequest request);
    AccountResponse toAccountCreateRequest(Account account);
}
