package com.mongoing.mongosaga;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Maxim Kalina
 * @version $Id$
 */
@Component
public class CompensateMethodInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CompensateMethodInterceptor.class);

    @Autowired
    private CompensationManager manager;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
         
        try {
            //log.info("---- Compensating Func {} ----", invocation.getMethod().toGenericString());
            Object ret = invocation.proceed();
            manager.enlist(invocation); // this step has been performed, subject to compensate
            return ret;
        } finally {           
            //log.info("---- END METHOD Compensating {} ----", invocation.getMethod().toGenericString());
        }

    }

}
