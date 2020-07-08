package com.my.conductor;

import org.hibernate.internal.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.event.EventListener;

import com.my.conductor.conductor.runner.thread.ConductorRunnerThreadProvider;
import com.my.conductor.environment.config.EnvironmentVariablesToSystemPropertiesMappingConfiguration;

@SpringBootApplication
@EnableZuulProxy
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, RestClientAutoConfiguration.class, CassandraAutoConfiguration.class})
public class SecureConductorBoot {
	
	private static String[] args_buffer;
	
	@Value("${conductor.startup.default:true}") 
	private String CONDUCTOR_STARTUP_DEFAULT;
	
	@Autowired
	EnvironmentVariablesToSystemPropertiesMappingConfiguration environmentVariablesToSystemPropertiesMappingConfiguration;
	
	public static void main(String[] args) throws InterruptedException
    {
        SpringApplication app = new SpringApplication(SecureConductorBoot.class);
        args_buffer = args;
        app.run(args);
	}
	
	@EventListener(ApplicationStartedEvent.class)
	public void mapEnvToProp()
	{
		this.environmentVariablesToSystemPropertiesMappingConfiguration.mapEnvToProp();
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void startConductorServer() throws InterruptedException {
		
		if(StringHelper.booleanValue(CONDUCTOR_STARTUP_DEFAULT))
		{
			ConductorRunnerThreadProvider conductorRunnerThread = ConductorRunnerThreadProvider.getInstance();
			
			conductorRunnerThread.configureArgs(args_buffer);
			conductorRunnerThread.setRunToTrue();
			conductorRunnerThread.start();  
		}
	}

}
