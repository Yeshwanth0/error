package com.eazybytes.accounts.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CardsDto;
import com.eazybytes.accounts.dto.CustomerDetailsDto;
import com.eazybytes.accounts.dto.LoansDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.ICustomersService;
import com.eazybytes.accounts.service.client.CardsFeignClient;
import com.eazybytes.accounts.service.client.LoansFeignClient;
@Service
public class CustomerServiceimpl implements ICustomersService {
     
	
	private AccountsRepository accountsRepository;
	private CustomerRepository customerRepository;
	private CardsFeignClient cardsFeignClient;
	private LoansFeignClient loansFeignClient;
	
	
	public CustomerServiceimpl(AccountsRepository accountsRepository, CustomerRepository customerRepository,
			CardsFeignClient cardsFeignClient, LoansFeignClient loansFeignClient) {
		super();
		this.accountsRepository = accountsRepository;
		this.customerRepository = customerRepository;
		this.cardsFeignClient = cardsFeignClient;
		this.loansFeignClient = loansFeignClient;
	}


	@Override
	public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
		// TODO Auto-generated method stub
		Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
		CustomerDetailsDto customerDetailsDto=CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
		customerDetailsDto.setAccountsdto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
		
		
		ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
		customerDetailsDto.setLoansdto(loansDtoResponseEntity.getBody());
		
		ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
		customerDetailsDto.setCardsdto(cardsDtoResponseEntity.getBody());
		
		
		return customerDetailsDto;
	}

}
