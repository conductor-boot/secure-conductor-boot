package com.my.conductor.db.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

@Configuration
@ConditionalOnProperty(
	    value="wrapper_db", 
	    havingValue = "mariadb4j", 
	    matchIfMissing = true)
public class EmbeddedMariaDbConfig {
	
	@Value("${mariadb4j.maxConnections:10000}")
	public String maxConnections;
	
	@Value("${mariadb4j.port:0}")
	public int mariadb4jPort;
	
	@Value("${mariadb4j.dataDir:NONE}")
	public String mariadb4jDataDir;
	
	@Value("${mariadb4j.libDir:NONE}")
	public String mariadb4jLibDir;
	
	@Value("${mariadb4j.baseDir:NONE}")
	public String mariadb4jBaseDir;
	
	@Value("${mariadb4j.deleteBaseAndDataDirOnShutdown:false}")
	public boolean deleteBaseAndDataDirOnShutdown;
	
	@Value("${mariadb4j.security.disabled:true}")
	public boolean securityDisabled;
	
	@Value("${mariadb4j.wait.timeout:18000000}")
	public String waitTimeout;
	
	@Value("${mariadb4j.connect.timeout:31536000}")
	public String connectTimeout;
	
    @Bean("mariaDB4jSpringService")
    public MariaDB4jSpringService mariaDB4jSpringService() {
    	
    	MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
    	
    	mariaDB4jSpringService.getConfiguration().addArg("--max-connections="+maxConnections);
    	mariaDB4jSpringService.getConfiguration().addArg("--wait-timeout="+waitTimeout);
    	mariaDB4jSpringService.getConfiguration().addArg("--connect-timeout="+connectTimeout);
    	
    	
    	if(null == mariadb4jDataDir || "NONE".equalsIgnoreCase(mariadb4jDataDir))
    		mariaDB4jSpringService.getConfiguration().setDeletingTemporaryBaseAndDataDirsOnShutdown(true);
    	else 
    		mariaDB4jSpringService.getConfiguration().setDeletingTemporaryBaseAndDataDirsOnShutdown(deleteBaseAndDataDirOnShutdown);
    	
    	mariaDB4jSpringService.getConfiguration().setSecurityDisabled(securityDisabled);
    	
    	if(mariadb4jPort>0)
    	{
    		mariaDB4jSpringService.setDefaultPort(mariadb4jPort);
    	}
    	
    	if(null != mariadb4jDataDir && !"NONE".equalsIgnoreCase(mariadb4jDataDir))
    	{
    		mariaDB4jSpringService.setDefaultDataDir(mariadb4jDataDir);
    	}
    	
    	if(null != mariadb4jLibDir && !"NONE".equalsIgnoreCase(mariadb4jLibDir))
    	{
    		mariaDB4jSpringService.setDefaultLibDir(mariadb4jLibDir);
    	}
    	
    	if(null != mariadb4jBaseDir && !"NONE".equalsIgnoreCase(mariadb4jBaseDir))
    	{
    		mariaDB4jSpringService.setDefaultBaseDir(mariadb4jBaseDir);
    	}
    	
        return mariaDB4jSpringService;
    }

    @Bean("datasource")
    @DependsOn("mariaDB4jSpringService")
    public DataSource dataSource(MariaDB4jSpringService mariaDB4jSpringService,
                          @Value("${mariadb4j.database.name:conductor}") String databaseName,
                          @Value("${spring.datasource.username:AnsiCon}") String datasourceUsername,
                          @Value("${spring.datasource.password:AnsiCon!23$}") String datasourcePassword,
                          @Value("${spring.datasource.driver-class-name:org.mariadb.jdbc.Driver}") String datasourceDriver) throws ManagedProcessException {
        //Create our database with default root user and no password
        mariaDB4jSpringService.getDB().createDB(databaseName);

        DBConfigurationBuilder config = mariaDB4jSpringService.getConfiguration();
        

        String databaseUrl = config.getURL(databaseName)+"?autoReconnect=true";
	    
	    System.setProperty("jdbc.url", databaseUrl);
        
        return DataSourceBuilder
                .create()
                .username(datasourceUsername)
                .password(datasourcePassword)
                .url(databaseUrl)
                .driverClassName(datasourceDriver)
                .build();
    }
}
