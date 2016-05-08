package com.mongoing.compensation;


import com.mongoing.compensation.Compensate;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountManager {

    private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
 
    
    @Compensate
    public void debit(String from, double amount){
    	log.info("debit " + amount+" from account "+from);
    }

    public void _undo_debit(String from, double amount){
        
    }
    
	@Compensate
    public void credit(String to, double amount){
    	log.info("credit "+ amount+" to "+to);
    }


}
