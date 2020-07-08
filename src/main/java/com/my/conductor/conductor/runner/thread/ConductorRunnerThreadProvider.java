package com.my.conductor.conductor.runner.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.conductor.bootstrap.Main;

public class ConductorRunnerThreadProvider extends Thread {
	
	private Logger logger = LoggerFactory.getLogger(ConductorRunnerThreadProvider.class.getSimpleName());
	
	private String[] args_buffer;
	
	private boolean run = false;
	
    private ConductorRunnerThreadProvider() {
    	
    }
    
    private static volatile ConductorRunnerThreadProvider INSTANCE;

    public static ConductorRunnerThreadProvider getInstance() {
           Object mutex = new Object();
           if(null == INSTANCE) {
                  synchronized(mutex) {
                        INSTANCE = new ConductorRunnerThreadProvider();
                  }
           }
           return INSTANCE;
    }
    
    public void configureArgs(String[] args) {
    	this.args_buffer = args;
    }
    
    public void setRunToTrue() {
    	this.run = true;
    }

	@Override
	public void run() {
		try {
			while(run)
			{			
				try {
					
					if(null!=args_buffer && args_buffer.length > 0)
				      {	
				        String[] args_new = new String[args_buffer.length-1];
				        
				        for(String arg: args_buffer)
				        {
				          if(!"--spring.output.ansi.enabled=always".equalsIgnoreCase(arg))
				          {
				            args_new[args_new.length] = arg;
				          }
				        }
				        
				        Main.main(args_new);
				      }
				      else
				      {	        
				        Main.main(args_buffer);
				    }
				}
				catch(IllegalArgumentException iEx)
				{
					if(iEx.getMessage().equalsIgnoreCase("No enum constant com.netflix.conductor.bootstrap.ModulesProvider.ExternalPayloadStorageType."))
					{
						logger.warn(iEx.getMessage());
					}
					else
					{
						logger.error(iEx.getMessage());
					}
				}
				catch(Exception e)
				{
					logger.error("############################# CANNOT START CONDUCTOR SERVER #############################");
				}	
			}
		}
		catch(Exception e)
		{
			logger.error("Running Thread "+e.getMessage());
		}
	}
	
	@SuppressWarnings("deprecation")
	public void stopThread()
	{
		System.out.println("############################# STOP CONDUCTOR SERVER #############################");
		run=false;
		try {
			logger.warn("############################# Thread Interrupt Triggerd #############################");
			this.interrupt();
			logger.warn("Thread State : "+this.getState().name().toString());
			logger.warn("############################# Thread Stop Triggerd #############################");
			this.stop();
			logger.warn("Thread State : "+this.getState().name().toString());
			System.out.println("Thread State : "+this.getState().name().toString());
		}
		catch(Exception e)
		{
			logger.error("Stopping Thread "+e.getMessage());
		}
	}
	
	public boolean isRunning() {
		return run;
	}
	
}
