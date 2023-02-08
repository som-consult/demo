/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibm.test.core.schedulers;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.test.core.processor.ReportProcessor;


/**
 * Multiple cron-jobs like tasks that get executed regularly.
 * It also demonstrates how property values can be set. Users can
 * set the property values in /system/console/configMgr
 */
@Component(immediate = true, service = Runnable.class)
@Designate(ocd = MultiSchedulerConfiguration.class)
public class MultiScheduler implements Runnable {
	
	
	@Reference
    private Scheduler scheduler;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiScheduler.class);
    private static final String BUK_SCH_ID = "bukscheduler";
    private static final String BCARD_SCH_ID = "bcardscheduler";
    
    
    // Added using OSGi Configuration Manager - Apache Sling Thread Pool Configuration
    private static final String RPT_THREAD_POOL = "report-generator-thread-pool";
    
    private String reportPathBuk;
    private String reportPathBcard;
    
    
    /**
     * This method gets triggered on bundle/service activation
     * @param config MultiSchedulerConfiguration
     */
    @Activate
    protected void activate(final MultiSchedulerConfiguration config) {
    	LOGGER.info("Multi Scheduler Activated.");
    	processScheduler(config);
    }
    
    /**
	 * Modifies the scheduler id on modification
	 * @param config MultiSchedulerConfiguration
	 */
	@Modified
	protected void modified(final MultiSchedulerConfiguration config) {
		LOGGER.info("Multi Scheduler Modified.");
		processScheduler(config);
	}
	
	/**
	 * This method processes Scheduling and Unscheduling
	 * @param config MultiSchedulerConfiguration
	 * @param event String
	 * 
	 * ------------ IMPORTANT ------------------
	 * By default the Scheduler will create Thread Pools with default settings (5 threads at maximum).
	 * But we need to configure them explicitly and adjust them to your needs.
	 * For this you need to instantiate the "Apache Sling Thread Pool Configuration" service factory
	 * and provide the name of the thread-pool as value of the OSGI property name.
	 * For us we might need to update it with min as 0 and max as 15.
	 */
	private void processScheduler(final MultiSchedulerConfiguration config) {
		
		LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		// Set configuration values for BUK and Add BUK Scheduler
		// Unschedule any existing schedule thread
    	// Begin new schedule with BUK Scheduler properties
    	if (config.isBukEnabled()) {
    		unscheduleScheduler(BUK_SCH_ID);
    		scheduleScheduler(config, BUK_SCH_ID);
    		LOGGER.info("BUK Scheduler is scheduled : {}", config.schedulerExpressionBuk());
        	LOGGER.info("BUK Report Path = {}", config.schedulerReportPathBuk());
    	} else {
    		LOGGER.info("BUK Scheduler is NOT scheduled since it is disabled.");
    	}
    	
    	
    	// Set configuration values for BCard & Add BCard Scheduler
    	// Unschedule any existing schedule thread
    	// Begin new schedule with BCard Scheduler properties
    	if (config.isBcardEnabled()) {
    		unscheduleScheduler(BCARD_SCH_ID);
    		scheduleScheduler(config, BCARD_SCH_ID);
    		LOGGER.info("BCard Scheduler is scheduled on Service Activation : {}", config.schedulerExpressionBcard());
        	LOGGER.info("BCard Report Path = {}", config.schedulerReportPathBcard());
    	} else {
    		LOGGER.info("BCard Scheduler is NOT scheduled since it is disabled.");
    	}
    	LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
	}
    
    
	/**
	 * This method adds the scheduler based on Enabled/Disabled Configuration
	 * @param config MultiSchedulerConfiguration
	 * @param schedulerId String
	 */
	private void scheduleScheduler(final MultiSchedulerConfiguration config, final String schedulerId) {

		ScheduleOptions scheduleOptions = null;
		
		if (StringUtils.contains(schedulerId, BUK_SCH_ID)) {
    		reportPathBuk = config.schedulerReportPathBuk();
			scheduleOptions = scheduler.EXPR(config.schedulerExpressionBuk());
			scheduleOptions.name(BUK_SCH_ID);
			scheduleOptions.canRunConcurrently(config.isSchedulerConcurrent());
			scheduleOptions.threadPoolName(RPT_THREAD_POOL);
			scheduler.schedule(this, scheduleOptions);
			LOGGER.info("BUK Scheduler has been successfully scheduled : {}", BUK_SCH_ID);
			LOGGER.info("BUK Scheduler report path : {}", config.schedulerReportPathBuk());
		}
		
		if (StringUtils.contains(schedulerId, BCARD_SCH_ID)) {
			reportPathBcard = config.schedulerReportPathBcard();
			scheduleOptions = scheduler.EXPR(config.schedulerExpressionBcard());
			scheduleOptions.name(BCARD_SCH_ID);
			scheduleOptions.canRunConcurrently(config.isSchedulerConcurrent());
			scheduleOptions.threadPoolName(RPT_THREAD_POOL);
			scheduler.schedule(this, scheduleOptions);
			LOGGER.info("BCard Scheduler has been successfully scheduled : {}", BCARD_SCH_ID);
			LOGGER.info("BCard Scheduler report path : {}", config.schedulerReportPathBcard());
		}
	}
	
	/**
	 * This method removes the scheduler based on Unique Scheduler ID
	 */
	private void unscheduleScheduler(String schedulerId) {
		// Removing the Scheduler
		scheduler.unschedule(schedulerId);
		LOGGER.info("Unscheduled Scheduler Job: {}", schedulerId);
	}
	
	/**
	 * Overridden run method to execute Job
	 */
	@Override
	public void run() {
		
		 // Thread safe local processor object dedicated to each thread
		ReportProcessor processor = new ReportProcessor();
		
		LOGGER.info("----------------------------------------------------------------------------");
		
		String schedulerThreadName = Thread.currentThread().getName();
		if (Thread.currentThread().isAlive() && !Thread.currentThread().isInterrupted()
				&& StringUtils.endsWith(schedulerThreadName, BUK_SCH_ID)) {
			LOGGER.info("BUK Scheduler is concurrently executing successfully : {}", schedulerThreadName);
			processor.processReport(reportPathBuk);
		}
		
		if (Thread.currentThread().isAlive() && !Thread.currentThread().isInterrupted()
				&& StringUtils.endsWith(schedulerThreadName, BCARD_SCH_ID)) {
			LOGGER.info("BCard Scheduler is concurrently executing successfully : {}", schedulerThreadName);
			processor.processReport(reportPathBcard);
		}
		
		// Fate of Threads is on them. They acquire locks on monitors which shouldn't be released
		// unless they have completed their whole execution cycle and released its acquired resources.
		// All we can do is to send it an interrupt signal, so that it would stop itself.
		// when its execution is over.
		Thread.currentThread().interrupt();
		
		LOGGER.info("Thread has done its job and is hence interrupted.");
		LOGGER.info("Active Thread count : {}", Thread.activeCount());
	}
	
	
    @Deactivate
    protected void deactivate(final MultiSchedulerConfiguration configuration) {
        
    	LOGGER.debug("Unscheduling BUK Scheduler: {}", BUK_SCH_ID);
    	unscheduleScheduler(BUK_SCH_ID);
        
        LOGGER.debug("Unscheduling Bcard Scheduler: {}", BCARD_SCH_ID);
        unscheduleScheduler(BCARD_SCH_ID);
    }
}
