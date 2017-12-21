package main;

import gui.App;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import services.CurrencyServiceImpl;
import threads.DownloaderThread;
import threads.FileProcessingThread;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by Iwo Skwierawski on 13.12.17.
 * Main class, where application starts
 * Contains objects used in global scope
 */
public class Global
{

    public static CurrencyServiceImpl currencyService;

    public static EntityManager em;

    public static App app;

    private static final Logger logger = Logger.getLogger(Global.class);

    public static void main(String[] args)
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit");
        em = emf.createEntityManager();
        currencyService = new CurrencyServiceImpl(em);
        BasicConfigurator.configure();
        app = new App();
        scheduleTasks();
    }

    /**
     * Executing after application starts, scheduling certain tasks to be executed on certain time.
     */
    private static void scheduleTasks()
    {
        scheduleDownload();
        scheduleProcessingFiles();
    }

    /**
     * Schedules to download currencies prices from nbp.pl at 4:00PM, once a day.
     */
    private static void scheduleDownload()
    {
        try
        {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            JobDetail download = JobBuilder.newJob(DownloaderThread.class).withIdentity("downloader", "fileProcess").build();
            Trigger downloadTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("downloadTrigger", "fileProcess")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 16 ? * MON-FRI *"))
                    .build();
            scheduler.scheduleJob(download, downloadTrigger);
            logger.info("Scheduled to download at " + downloadTrigger.getNextFireTime());
        }
        catch (SchedulerException e)
        {
            logger.error("Error while scheduling tasks", e);
        }
    }

    /**
     * Schedules to process downloaded xml files and save informations, that they contain to DB.
     */
    private static void scheduleProcessingFiles()
    {
        try
        {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            JobDetail download = JobBuilder.newJob(FileProcessingThread.class).withIdentity("processor", "fileProcess").build();
            Trigger downloadTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("processorTrigger", "fileProcess")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 * * ? * MON-FRI *"))
                    .build();
            scheduler.scheduleJob(download, downloadTrigger);
            logger.info("Scheduled to process files at " + downloadTrigger.getNextFireTime());
        }
        catch (SchedulerException e)
        {
            logger.error("Error while scheduling tasks", e);
        }
    }
}
