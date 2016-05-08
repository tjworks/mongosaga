package com.mongoing.compensation;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * @author Maxim Kalina
 * @version $Id$
 */
@Component
public class CompensateMethodInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CompensateMethodInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
         
        try {
            log.info("---- Compensating Func {} ----", invocation.getMethod().toGenericString());
            return invocation.proceed();
        } finally {
           
            log.info("---- END METHOD Compensating {} ----", invocation.getMethod().toGenericString());
        }

    }

}
