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
    private AccountManager bean;
    @Autowired
    private MongoClient mongo ;

    DBCollection accounts;
 	 
     
    @Before
    public void resetAccountData() throws Exception {
        //MongoClient mongo = new MongoClient("localhost", 27017);
        DB database = mongo.getDB("test");

        database.getCollection("accounts").drop();
        accounts = database.getCollection("accounts");

        accounts.insert(new BasicDBObject("name", "tj").append("balance", 10000.0));
        accounts.insert(new BasicDBObject("name", "mona").append("balance",10000.0));
    }

    /**
     * Transfer 100 from A to B and assert that it was successful.
     *
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {
        //Logger.getRootLogger().removeAllAppenders();
        final int[] counter = new int[]{0};
        
        ExecutorService executor = Executors.newFixedThreadPool(64);
        Date start = new Date();
        for (int i = 0; i < 64; i++) {
            Runnable worker = new Runnable(){
                public void run(){
                    int amount = (int)(Math.random()*510);
                    for(int k=0;k<1000;k++){
                        try{
                            if(amount % 2 ==0 )
                                bean.transfer("mona", "tj", amount);
                            else
                                bean.transfer("tj", "mona", amount);    
                        }
                        catch(Exception e){
                            counter[0]++;
                        }    
                    }                    
                    System.out.println("Transfering "+ amount);
                }
            };
            executor.execute(worker);
        } 
        shutdownAndAwaitTermination(executor);
        System.out.println("#### Time used: " +(start.getTime() - new Date().getTime())/1000);
        System.out.println("#### Total compensations: "+ counter[0]);
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
        DBObject accountDoc1 = accounts.findOne(new BasicDBObject("name", "mona"));
        DBObject accountDoc2 = accounts.findOne(new BasicDBObject("name", "tj"));
        Double actualBalance1 = (Double) accountDoc1.get("balance");
        Double actualBalance2 = (Double) accountDoc2.get("balance");

        Assert.assertEquals("Total Balance is not as expected. Got '" + (actualBalance1.intValue()+actualBalance2.intValue() )
            + "', expected:  20000", 20000, (actualBalance1.intValue()+actualBalance2.intValue() ), 0);
    }
}
