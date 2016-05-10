package com.mongoing.mongosaga;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class CompensationTest {

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

        accounts.insert(new BasicDBObject("name", "tj").append("balance", 1000.0));
        accounts.insert(new BasicDBObject("name", "mona").append("balance", 1000.0));
    }

    /**
     * Transfer 100 from A to B and assert that it was successful.
     *
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception {

        //for(int i=0;i<3;i++)
        bean.transfer("mona", "tj", 100.0);
        assertBalance("mona", 900.0);
        assertBalance("tj", 1100.0);
    }

    /**
     * Attempt to transfer £600 from A to B. The banking service will fail this transfer due to the amount being
     * above the transfer limit.
     *
     * The test asserts that both balances are set to £1000 after the transaction fails.
     *
     * @throws Exception
     */
    @Test
    public void testFailure() throws Exception {

        //Initiate a 'high value' transfer that will fail
        try {
            bean.transfer("mona", "tj", 600.0);
            Assert.fail("Expected a TransactionCompensatedException to be thrown");
        } catch (Exception e) {
            //expected
            System.out.println("Expected error: "+ e.getMessage());
        }
        assertBalance("mona", 1000.0);
        assertBalance("tj", 1000.0);
    }

    /**
     * Simple helper method that requests a user's account document and asserts that the balance is as expected.
     *
     * @param account The account name, used to lookup the right account document.
     * @param expectedBalance The expected balance
     */
    private void assertBalance(String account, Double expectedBalance) {
        DBObject accountDoc = accounts.findOne(new BasicDBObject("name", account));
        Double actualBalance = (Double) accountDoc.get("balance");
        Assert.assertEquals("Balance is not as expected. Got '" + actualBalance + "', expected: '" + expectedBalance + "'", expectedBalance, actualBalance, 0);
    }
}
