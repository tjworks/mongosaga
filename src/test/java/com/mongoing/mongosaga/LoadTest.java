package com.mongoing.mongosaga;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import org.slf4j.Logger;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class LoadTest {

    @Autowired
    private AccountManager accountManager;
    @Autowired
    private MongoClient mongo ;

    DBCollection accounts;
 	 
     
    @Before
    public void resetAccountData() throws Exception {
        //MongoClient mongo = new MongoClient("localhost", 27017);
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        DB database = mongo.getDB("test");

        database.getCollection("accounts").drop();
        accounts = database.getCollection("accounts");
        for(int i=0;i<100;i++){

            accounts.insert(new BasicDBObject("name", "tj"+i).append("balance", 0.0));
            accounts.insert(new BasicDBObject("name", "mona"+i).append("balance",0.0));    
        }   
        accounts.insert(new BasicDBObject("name", "tj").append("balance", 0.0));
        accounts.insert(new BasicDBObject("name", "mona").append("balance",0.0));         
    }

    /**
     * Transfer 100 from A to B and assert that it was successful.
     *
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {
        
        if(true) return;
        
        final int[] counter = new int[]{0, 0};
        
        final class Worker implements Runnable {
            int id;
            public Worker(int id){ this.id=id; }
            public void run(){
                int amount = (int)(Math.random()*100)+1;
                for(int k=0;k<1000;k++){
                    try{
                        counter[0]++;
                        accountManager.transfer("mona"+ id, "tj"+ id, amount);                            
                    }
                    catch(Exception e){ counter[1]++; }    
                }                                 
            }
        }        
        int poolSize = 100;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);        
        for (int i = 0; i < poolSize; i++) {             
            executor.execute(new Worker(i));
        } 
        Thread.sleep(3);
        Date start = new Date();
        shutdownAndAwaitTermination(executor);
        System.out.println("#### Time used: " +( new Date().getTime()-start.getTime() )/1000);
        System.out.println("#### Total compensations: "+ counter[1] +" out of total op: "+ counter[0]);
        assertBalance();
        

    }   
    
    
    private void shutdownAndAwaitTermination(ExecutorService pool) {
       pool.shutdown(); // Disable new tasks from being submitted
       try {
         // Wait a while for existing tasks to terminate
         if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
           pool.shutdownNow(); // Cancel currently executing tasks
           // Wait a while for tasks to respond to being cancelled
           if (!pool.awaitTermination(60, TimeUnit.SECONDS))
               System.err.println("Pool did not terminate");
         }
       } catch (InterruptedException ie) {
         // (Re-)Cancel if current thread also interrupted
        System.err.println("Interrupted");
         pool.shutdownNow();
         // Preserve interrupt status
         Thread.currentThread().interrupt();
       }
     }
    /**
     * Simple helper method that requests a user's account document and asserts that the balance is as expected.
     *
     * @param account The account name, used to lookup the right account document.
     * @param expectedBalance The expected balance
     */
    private void assertBalance() {

        
        DBObject sum = new BasicDBObject("_id", "").append("sum", new BasicDBObject("$sum", "$balance"));        
        DBObject group = new BasicDBObject("$group", sum);

        BasicDBObject output = (BasicDBObject)(accounts.aggregate(group).results().iterator().next());

        Assert.assertEquals("Total Balance is not as expected. Got '" + output.get("sum")
            + "', expected:  0", 0, output.getInt("sum"), 0);
    }
}
