## Mongosaga 



## How to use

#### Step 1
Declare dependancy if you're using Maven:

	<dependency>
         <groupId>com.mongoing.mongosaga</groupId>
         <artifactId>mongosaga</artifactId>
         <version>0.5</version>            
     </dependency>

Or download mongosaga-x.xjar file directly and include in the classpath. 

#### Step 2
In your Spring applicationContext file, add following lines(if not already exists):

	<context:component-scan base-package="com.mongoing.mongosaga"/>        
    <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator">
        <property name="proxyTargetClass" value="true"/>
    </bean>
 	  
## About logging

To turn on debugging, either using the system property or a property file

System property: 

	mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

Or create a  **simplelogger.properties** file under classpath folder and include following line:

	org.slf4j.simpleLogger.defaultLogLevel=info



