package com.mongoing.mongosaga;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import javax.annotation.PostConstruct;
import com.mongoing.mongosaga.Compensate;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Account {

    private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
    private DB database;
    private DBCollection accounts;

    @Autowired
    private MongoClient mongo ;

    
    @PostConstruct
    private void init(){
        database = mongo.getDB("test");
        accounts = database.getCollection("accounts");
    }

    @Compensate
    public void debit(String from, double amount){
    	log.info("debit " + amount+" from account "+from);
        accounts.update(new BasicDBObject("name", from), 
                        new BasicDBObject("$inc", new BasicDBObject("balance", -1* amount)));
    }

    public void debit_compensator(String from, double amount){
        log.info("debit compensation, adding " + amount+" back to "+from);   
        accounts.update(new BasicDBObject("name", from), 
                        new BasicDBObject("$inc", new BasicDBObject("balance",  amount)));
    }
    
	@Compensate
    public void credit(String to, double amount){
    	log.info("credit "+ amount+" to "+to);
        //High value transfers (over 500) are not allowed with this service
        if (amount > 500) {
            //Mark the current transaction as 'compensateOnly'. This ensures that the transaction will fail.            
            throw new RuntimeException("Invalid amount");            
        }
        accounts.update(new BasicDBObject("name", to), 
                        new BasicDBObject("$inc", new BasicDBObject("balance", amount)));
    }

    public void credit_compensator(String to, double amount){
        log.info("credit compensation, subtract " + amount+" from  "+to);   
        accounts.update(new BasicDBObject("name", to), 
                        new BasicDBObject("$inc", new BasicDBObject("balance", -1* amount)));
    }

}
