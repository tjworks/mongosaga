package com.mongoing.mongosaga;


import com.mongoing.mongosaga.Compensatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountManager {

    private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
 
 	@Autowired
    private Account account;

 	

    @Compensatable
    public void transfer(String from, String to, double amount) {
        //log.info("Executing method 'AccountService.transfer'.");
         account.debit(from, amount);
         account.credit(to, amount);        
    }
 


}
