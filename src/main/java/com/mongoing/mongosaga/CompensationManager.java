package com.mongoing.mongosaga;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aopalliance.intercept.MethodInvocation;



@Component
public class CompensationManager{
	private static final Logger log = LoggerFactory.getLogger(CompensationManager.class);
	static final ThreadLocal userThreadLocal = new ThreadLocal();
    static final AtomicLong idGenerator = new AtomicLong(10000);
    public CompensatableTx startTx() {
        long uid = idGenerator.getAndIncrement();
    	CompensatableTx tx = new CompensatableTx(uid);
        userThreadLocal.set(tx);
        log.debug("TX"+tx.getId()+" started.");
        return tx;
    }

    public void resetTx() {
        userThreadLocal.remove();
    }
    public void enlist(MethodInvocation invocation){
    	getTx().add(invocation);
    }
    public long getTransactionId(){
        return getTx().getId();
    }
    public boolean isCompensating(){
        return getTx().isCompensating();
    }
    public CompensatableTx getTx() {
        return (CompensatableTx)userThreadLocal.get();
    }
    void doCompensation(){
    	CompensatableTx tx = getTx();
    	List<MethodInvocation> invocations = tx.getInvocations();
        tx.setCompensating(true);
    	for(int i=invocations.size()-1;i>=0;i--){
    		MethodInvocation invocation = invocations.get(i);
    		log.debug("Compensating {}", invocation.getMethod().toGenericString());
    		Method compensator = null;
    		Object target = invocation.getThis();
    		Method[] methods = target.getClass().getMethods();
    		for(int k=0; methods!=null && k<methods.length;k++){
    			Method m = methods[k];
    			if(m.getName().equals( invocation.getMethod().getName()+"_compensator")){
    				compensator = m;
    				break;
    			}    				
    		}
    		if(compensator == null)
    			log.warn("Compensator for {}"+ invocation.getMethod().getName() +" does not exist.");
    		else{
    			try{
    				compensator.invoke(target,invocation.getArguments() );	
    			}
    			catch(IllegalAccessException ile){
    				/**@todo: ignorable */
    				log.error(ile.toString());
    			}
    			catch(java.lang.reflect.InvocationTargetException ite){
    				/**@todo: ignorable */
    				log.error(ite.toString());	
    			}
    			
    		}
    	}
    }
}

class CompensatableTx {
	private static final Logger log = LoggerFactory.getLogger(CompensatableTx.class);
    
	private long id;
    private boolean compensating;
	private ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>(5);
    public CompensatableTx(long uid){
        this.id = uid;
    }
	public long getId(){
		return id;
	}
	public void add(MethodInvocation invocation){
        /**@todo: check log level first */
		log.trace("TX"+getId()+": ---- enlisting {} ----", invocation.getMethod().toGenericString());
		invocations.add(invocation);
	}
	public ArrayList<MethodInvocation> getInvocations(){
		return invocations;
	}
    void setCompensating(boolean status){
        this.compensating = status;
    }
    boolean isCompensating(){
        return true;
    }
}