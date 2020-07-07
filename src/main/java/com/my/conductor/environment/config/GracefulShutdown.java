package com.my.conductor.environment.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.my.conductor.conductor.runner.thread.ConductorRunnerThreadProvider;

public class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
	
	private static final Logger log = LoggerFactory.getLogger(GracefulShutdown.class);
	
	private volatile Connector connector;
	
	@Value("${awaitTermination:120}")
	private int awaitTermination;

	@Override
	public void customize(Connector connector) {
		this.connector = connector;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		this.connector.pause();
		ConductorRunnerThreadProvider.getInstance().stopThread();
		
		Executor executor = this.connector.getProtocolHandler().getExecutor();
		if (executor instanceof ThreadPoolExecutor) {
			try {
				ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
				threadPoolExecutor.shutdown();
				if (!threadPoolExecutor.awaitTermination(awaitTermination, TimeUnit.SECONDS)) {
					log.warn("Conductor Boot thread pool did not shut down gracefully within " + awaitTermination
							+ " seconds. Proceeding with forceful shutdown");
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
