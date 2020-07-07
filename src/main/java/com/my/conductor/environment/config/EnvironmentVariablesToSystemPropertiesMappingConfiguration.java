package com.my.conductor.environment.config;

import java.util.Iterator;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
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
	}

}
