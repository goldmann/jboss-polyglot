/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.polyglot.jobs;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.projectodd.polyglot.core.util.TimeInterval;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class BaseScheduledJob implements Service<BaseScheduledJob>, BaseScheduledJobMBean {
    
    public BaseScheduledJob(Class<? extends Job> jobClass, String group, String name, String description, String cronExpression, boolean singleton) {    
        this( jobClass, group, name, description, cronExpression, null, singleton );
    }
    
    public BaseScheduledJob(Class<? extends Job> jobClass, String group, String name, String description, String cronExpression, TimeInterval timeout, boolean singleton) {
        this.group = group;
        this.name = name;
        this.description = description;
        this.cronExpression = cronExpression;
        this.timeout = timeout;
        this.singleton = singleton;
        this.jobClass = jobClass;
    }
    
    @Override
    public BaseScheduledJob getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        
        context.execute(new Runnable() {
            public void run() {
                try {
                    BaseScheduledJob.this.start();
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        });
    }
    
    @Override
    public void stop(StopContext context) {
        stop();
    }
   
    public synchronized void start() throws ParseException, SchedulerException {
        this.jobDetail = new JobDetail();
        this.jobDetail.setGroup( this.group );
        this.jobDetail.setName( this.name );
        this.jobDetail.setDescription( this.description );
        this.jobDetail.setJobClass( this.jobClass );
        this.jobDetail.setRequestsRecovery( true );
        this.jobDetail.getJobDataMap().put("timeout", timeout);
        
        CronTrigger trigger = new CronTrigger( getTriggerName(), this.group, this.cronExpression );

        Scheduler scheduler = getScheduler();

        scheduler.scheduleJob( jobDetail, trigger );

        if (this.timeout != null &&
                this.timeout.interval > 0 && 
                scheduler.getGlobalTriggerListener( BaseTriggerListener.TRIGGER_LISTENER_NAME ) == null) {
            scheduler.addGlobalTriggerListener(new BaseTriggerListener());
        }
    }

    public synchronized void stop() {
        try {
            getScheduler().unscheduleJob( getTriggerName(), this.group );
        } catch (SchedulerException ex) {
            log.warn( "An error occurred stoping job " + this.name, ex );
        } 
        this.jobDetail = null;  
    }
    
    public Scheduler getScheduler() {
        return this.jobSchedulerInjector.getValue().getScheduler();
    }
    

    private String getTriggerName() {
        return this.name + ".trigger";
    }

    public synchronized boolean isStarted() {
        return this.jobDetail != null;
    }
    
    public synchronized boolean isStopped() {
        return this.jobDetail == null;
    }
    
    public synchronized String getStatus() {
        if ( isStarted() ) {
            return "STARTED";
        }
        
        return "STOPPED";
    }
    
    public boolean isSingleton() {
        return singleton;
    }

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public Injector<BaseJobScheduler> getJobSchedulerInjector() {
        return this.jobSchedulerInjector;
    }
    
    public void setTimeout(TimeInterval timeout) {
        this.timeout = timeout;
    }
    
    /**
     * Sets the timeout in seconds.
     */
    public void setTimeout(long timeout) {
        this.timeout = new TimeInterval( timeout, TimeUnit.SECONDS );
    }
    
    private InjectedValue<BaseJobScheduler> jobSchedulerInjector = new InjectedValue<BaseJobScheduler>();
    
    private Class<? extends Job> jobClass;
    
    private String group;
    private String name;
    private String description;

    private String cronExpression;
    private TimeInterval timeout;
    
    private JobDetail jobDetail;
    private boolean singleton;

    private static final Logger log = Logger.getLogger( "org.projectodd.polyglot.jobs" );
}
