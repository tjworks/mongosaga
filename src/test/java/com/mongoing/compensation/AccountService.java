package com.mongoing.compensation;


import com.mongoing.compensation.Compensatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
 
 	@Autowired
    private AccountManager mgr;

 	

    @Compensatable
    public void transfer(String from, String to, double amount) {
        log.info("Executing method 'AccountService.transfer'.");
         mgr.debit(from, amount);
         mgr.credit(to, amount);        
    }
 


}
