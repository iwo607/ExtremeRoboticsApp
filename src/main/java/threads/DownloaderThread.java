package threads;


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Thread responsible for downloading current currency prices from nbp.pl and save them to files
 */
public class DownloaderThread implements Job
{

    private static final Logger logger = Logger.getLogger(DownloaderThread.class);

    public void execute(JobExecutionContext context)
    {
        downloadFiles();
    }


    /**
     * This method is used to download actual currencies prices and save them in .xml format.
     * Executed by DownloaderThread each day at 1:00AM (prices are updated once per day).
     * It also downloads archived prices from current year in .csv format.
     */
    private static void downloadFiles()
    {

        try
        {
            URL url1 = new URL("http://www.nbp.pl/kursy/xml/LastA.xml");
            FileUtils.copyURLToFile(url1, new File("files/import/LastA.xml"));
        }
        catch (MalformedURLException e)
        {
            logger.error("Url error!", e);
        }
        catch (IOException e)
        {
            logger.error("Error writing to file!", e);
        }
        try
        {
            URL url2 = new URL("http://www.nbp.pl/kursy/xml/LastB.xml");
            FileUtils.copyURLToFile(url2, new File("files/import/LastB.xml"));
        }
        catch (MalformedURLException e)
        {
            logger.error("Url error!", e);
        }
        catch (IOException e)
        {
            logger.error("Error writing to file!", e);
        }
        try
        {
            URL url2 = new URL("http://www.nbp.pl/kursy/Archiwum/archiwum_tab_a_"+DateTime.now().getYear()+".csv");
            FileUtils.copyURLToFile(url2, new File("files/import/ArchiveA-"+DateTime.now().getYear()+".csv"));
        }
        catch (MalformedURLException e)
        {
            logger.error("Url error!", e);
        }
        catch (IOException e)
        {
            logger.error("Error writing to file!", e);
        }
        try
        {
            URL url2 = new URL("http://www.nbp.pl/kursy/Archiwum/archiwum_tab_b_"+DateTime.now().getYear()+".csv");
            FileUtils.copyURLToFile(url2, new File("files/import/ArchiveB-"+DateTime.now().getYear()+".csv"));
        }
        catch (MalformedURLException e)
        {
            logger.error("Url error!", e);
        }
        catch (IOException e)
        {
            logger.error("Error writing to file!", e);
        }
    }
}

