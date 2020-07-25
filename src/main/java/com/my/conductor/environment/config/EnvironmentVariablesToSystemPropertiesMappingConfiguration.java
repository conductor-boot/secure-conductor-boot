package com.my.conductor.environment.config;

import java.util.Iterator;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

@Configuration
@SuppressWarnings({ "rawtypes" })
public class EnvironmentVariablesToSystemPropertiesMappingConfiguration {

	@Autowired
	Environment environment;

	@Autowired
	ApplicationContext ctx;
	
	@Value("${ELASTICSEARCH_HOST:elasticsearch}")
	private String ELASTICSEARCH_HOST;
	
	@Value("${ELASTICSEARCH_PORT:0}")
	private int ELASTICSEARCH_PORT;

	public void mapEnvToProp() {

		Properties props = new Properties();

		for (Iterator it = ((AbstractEnvironment) ctx.getEnvironment()).getPropertySources().iterator(); it.hasNext();) 
		{	
			PropertySource propertySource = (PropertySource) it.next();
			
			if (propertySource instanceof OriginTrackedMapPropertySource) 
			{
				props.putAll(((MapPropertySource) propertySource).getSource());
			}
		}
		
		props.forEach((type, value) -> {
			System.setProperty(String.valueOf(type), String.valueOf(value));
		});
		
		if(ELASTICSEARCH_PORT > 0)
		{
			String url = "http://"+ELASTICSEARCH_HOST+":"+ELASTICSEARCH_PORT;
			
			System.setProperty("workflow.elasticsearch.url", url);
			
			System.out.println("~~~~~~~~~~~~ES URL SET FOR CONDUCTOR - EXTERNAL~~~~~~~~~~");
		}
		
	}

}
