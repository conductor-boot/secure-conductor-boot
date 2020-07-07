package com.my.conductor.db.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
	    value="wrapper_db", 
	    havingValue = "mysql", 
	    matchIfMissing = false)
public class MySQLDbConfig {
	
	@Bean("datasource")
    public DataSource dataSource(
                          @Value("${spring.datasource.username:AnsiCon}") String datasourceUsername,
                          @Value("${spring.datasource.password:AnsiCon!23$}") String datasourcePassword,
                          @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/oauth?createDatabaseIfNotExist=true&autoReconnect=true&verifyServerCertificate=false&useSSL=false}") String datasourceUrl,
                          @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}") String datasourceDriver){
        

        return DataSourceBuilder
                .create()
                .username(datasourceUsername)
                .password(datasourcePassword)
                .url(datasourceUrl)
                .driverClassName(datasourceDriver)
                .build();
    }

}
