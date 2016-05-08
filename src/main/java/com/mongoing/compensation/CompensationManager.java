package com.mongoing.compensation;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aopalliance.intercept.MethodInvocation;



@Component
public class CompensationManager{
	private static final Logger log = LoggerFactory.getLogger(CompensationManager.class);
	static final ThreadLocal userThreadLocal = new ThreadLocal();

    public CompensatableTx startTx() {
    	CompensatableTx tx = new CompensatableTx();
        userThreadLocal.set(tx);
        log.info("TX"+tx.getId()+" started.");
        return tx;
    }

    public void resetTx() {
        userThreadLocal.remove();
    }
    public void enlist(MethodInvocation invocation){
    	getTx().add(invocation);
    }
    public CompensatableTx getTx() {
        return (CompensatableTx)userThreadLocal.get();
    }
    public void rollback(){
    	CompensatableTx tx = getTx();
    	List<MethodInvocation> invocations = tx.getInvocations();
    	for(int i=invocations.size()-1;i>=0;i--){
    		MethodInvocation invocation = invocations.get(i);
    		log.info("Compensating {}", invocation.getMethod().toGenericString());
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
	private long id = (long) (Math.random()*10000000); //@todo: unique
	private ArrayList<MethodInvocation> invocations = new ArrayList<MethodInvocation>(5);
	public long getId(){
		return id;
	}
	public void add(MethodInvocation invocation){
		log.info("TX"+getId()+": ---- enlisting {} ----", invocation.getMethod().toGenericString());
		invocations.add(invocation);
	}
	public ArrayList<MethodInvocation> getInvocations(){
		return invocations;
	}
}