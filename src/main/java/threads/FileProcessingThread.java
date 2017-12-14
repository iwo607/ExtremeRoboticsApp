package threads;

import main.Global;
import models.CurrencyPrice;
import models.xml.PriceTable;
import models.xml.XMLCurrency;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Thread responsible for checking if source files changed and if they did - process them and save to DB.
 */
public class FileProcessingThread implements Job
{

    private static final Logger logger = Logger.getLogger(FileProcessingThread.class);
    private static Map<String, PriceTable> tables = new HashMap<>();                                                    // Key: tableName, Value: table matching that name

    public void execute(JobExecutionContext context)
    {
        processDownloadedFiles();
    }

    /**
     * This method is executed each minute to check if there are new files to process.
     * If there are, it converts them to objects and saves to DB, then deleting outdated files.
     */
    private static void processDownloadedFiles()
    {
        File fileToProcess = new File("files/import/LastA.xml");
        if(!fileToProcess.exists())
            return;
        Map<String, models.Currency> currencyMap = new HashMap<>();                                                     // Map containing all currencies, accessed by their unique code
        Global.currencyService.getAllTables().stream().forEach(table ->
        {
            tables.put(table.getTableName(), table);
            table.getCurrencies().stream().forEach(currency -> currencyMap.put(currency.getCurrencyCode(), currency));
        });
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        EntityManager em = Global.em;
        try
        {
            InputStream inputStream = new FileInputStream("files/import/LastA.xml");
            Reader reader = new InputStreamReader(inputStream, "ISO-8859-2");
            JAXBContext context = JAXBContext.newInstance(PriceTable.class);
            Unmarshaller un = context.createUnmarshaller();

            PriceTable tableA = (PriceTable)un.unmarshal(reader);
            if(!tables.containsKey("A") || !tableA.getPublicationDate().equals(tables.get("A").getPublicationDate()))
            {
                em.getTransaction().begin();
                List<XMLCurrency> currencies = tableA.getXmlCurrencies();
                currencies.stream().forEach(currency -> {                                                                   // Basing on processed xml file, it creates new currency, or updates existing with new values.
                    models.Currency currencyToUpdate;
                    if(currencyMap.containsKey(currency.getCurrencyCode()))
                        currencyToUpdate = currencyMap.get(currency.getCurrencyCode());
                    else
                        currencyToUpdate = new models.Currency(currency);
                    CurrencyPrice newPrice = new CurrencyPrice(currency.getAvgPrice());
                    DateTime priceForDate = formatter.parseDateTime(tableA.getPublicationDate());
                    newPrice.setDate(priceForDate);
                    currencyToUpdate.getAvgPrices().add(newPrice);
                    if(!tables.containsKey("A"))
                    {
                        tableA.getCurrencies().add(currencyToUpdate);
                    }
                });
                if(!tables.containsKey("A"))
                {
                    tableA.setTableName("A");
                    tableA.save(em);
                }
                else
                {
                    PriceTable table = tables.get("A");
                    table.setPublicationDate(tableA.getPublicationDate());
                    table.save(em);
                }
            }

        }
        catch (JAXBException e)
        {
            logger.error("Couldn't convert xml to object.", e);
        }

        catch (IOException e)
        {
            logger.error("Error while processing file!", e);
        }
        fileToProcess = new File("files/import/LastB.xml");
        if(!fileToProcess.exists())
            return;
        try
        {
            InputStream inputStream = new FileInputStream("files/import/LastB.xml");
            Reader reader = new InputStreamReader(inputStream, "ISO-8859-2");
            JAXBContext context = JAXBContext.newInstance(PriceTable.class);
            Unmarshaller un = context.createUnmarshaller();

            PriceTable tableB = (PriceTable)un.unmarshal(reader);
            if(!tables.containsKey("B") || !tableB.getPublicationDate().equals(tables.get("B").getPublicationDate())) {
                if(!em.getTransaction().isActive())
                    em.getTransaction().begin();
                List<XMLCurrency> currencies = tableB.getXmlCurrencies();
                currencies.stream().forEach(currency -> {
                    models.Currency currencyToUpdate;
                    if (currencyMap.containsKey(currency.getCurrencyCode()))
                        currencyToUpdate = currencyMap.get(currency.getCurrencyCode());
                    else
                        currencyToUpdate = new models.Currency(currency);
                    CurrencyPrice newPrice = new CurrencyPrice(currency.getAvgPrice());
                    DateTime priceForDate = formatter.parseDateTime(tableB.getPublicationDate());
                    newPrice.setDate(priceForDate);
                    currencyToUpdate.getAvgPrices().add(newPrice);
                    if(!tables.containsKey("B"))
                    {
                        tableB.getCurrencies().add(currencyToUpdate);
                    }
                });
                if(!tables.containsKey("B"))
                {
                    tableB.setTableName("B");
                    tableB.save(em);
                }
                else
                {
                    PriceTable table = tables.get("B");
                    table.setPublicationDate(tableB.getPublicationDate());
                    table.save(em);
                }
            }
        }
        catch (JAXBException e)
        {
            logger.error("Couldn't convert xml to object.", e);
        }
        catch (IOException e)
        {
            logger.error("Error while processing file!", e);
        }
        if(em.getTransaction().isActive())
        {
            try
            {
                em.getTransaction().commit();
                Global.app.connectDataSources();
            }
            catch (RuntimeException e)
            {
                logger.error("Error while commiting transaction!", e);
            }
            logger.info("Saved new prices to DB.");
        }
    }
}
