package com.example.identityservice.service;

import com.example.dtocommon.kafka.User_Account.CustomerCreateEvent;
import com.example.identityservice.dto.request.AccountCreateRequest;
import com.example.identityservice.dto.request.AccountUpdateRequest;
import com.example.identityservice.dto.request.ChangePasswordRequest;
import com.example.identityservice.dto.response.AccountResponse;
import com.example.identityservice.dto.response.ApiResponse;
import com.example.identityservice.entity.Account;
import com.example.identityservice.entity.Role;
import com.example.identityservice.entity.User;
import com.example.identityservice.enums.ErrorCode;
import com.example.identityservice.enums.TypeOfRole;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.mapper.AccountMapper;
import com.example.identityservice.repository.AccountRepository;
import com.example.identityservice.repository.RoleRepository;
import com.example.identityservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    ObjectMapper objectMapper;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    KafkaTemplate<String, String> kafkaTemplate;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

//    public AccountResponse createCustomerAccount(AccountCreateRequest request) {
//        if (accountRepository.existsByUsername(request.getUsername())) {
//            throw new AppException(ErrorCode.EXISTED);
//        }
//        Account account = accountMapper.toAccount(request);
//        account.setPassword(passwordEncoder.encode(account.getPassword()));
//        var roles = request.getRoles();
//        Set<Role> roleSet = new HashSet<>();
//        roles.forEach(role ->
//                roleSet.add(roleRepository.findByTypeOfRole(TypeOfRole.valueOf(role)))
//        );
//        account.setRoles(roleSet);
//        User user = userRepository.findById(request.getUser()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
//        account.setUser(user);
//        accountRepository.save(account);
//        kafkaTemplate.send("account-create", account.getId());
//        return accountMapper.toAccountResponse(account);
//    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(accountMapper::toAccountResponse).toList();
    }

    public AccountResponse updateAccount(String accountID, AccountUpdateRequest request) {
        Account account = accountRepository.findById(accountID).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        accountMapper.updateAccount(request, account);
        var roles = request.getRoles();
        Set<Role> roleSet = new HashSet<>();
        roles.forEach(role ->
                roleSet.add(roleRepository.findByTypeOfRole(TypeOfRole.valueOf(role)))
        );
        account.setRoles(roleSet);
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    public void deleteAccount(String accountID) {
        accountRepository.deleteById(accountID);
    }

    @KafkaListener(topics = "user-create", groupId = "account-group")
    public void handleEventUserCreate(@Payload String message) throws JsonProcessingException {
        CustomerCreateEvent user = objectMapper.readValue(message, CustomerCreateEvent.class);
        Account account = new Account();
        account.setUsername(user.getEmail());
        account.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByTypeOfRole(TypeOfRole.CUSTOMER);
        account.setRoles(Set.of(role));
        account.setUser(userRepository.findById(user.getId()).get());
        accountRepository.save(account);
        kafkaTemplate.send("account-create", account.getId());
    }
    public ApiResponse<?> changePassword(ChangePasswordRequest request) {
        boolean check = passwordEncoder.matches(request.getOldPassword(), accountRepository.findById(request.getAccountID()).get().getPassword());
        if (check) {
            Account account = accountRepository.findById(request.getAccountID()).get();
            account.setPassword(passwordEncoder.encode(request.getNewPassword()));
            accountRepository.save(account);
            return ApiResponse.builder()
                    .message("Password changed")
                    .build();
        }
        throw new AppException(ErrorCode.OLD_PASSWORD_MISMATCH);
    }
}
