package threads;

import main.Global;
import models.Currency;
import models.CurrencyPrice;
import models.xml.PriceTable;
import models.xml.XMLCurrency;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
     * Process downloaded archive file if this is first run and there are no entries in DB
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
        EntityManager em = Global.em;
        if(tables.size() == 0)
        {
            em.getTransaction().begin();
            File archive = new File("files/import/ArchiveA-"+DateTime.now().getYear()+".csv");
            if(archive.exists())
            {
                PriceTable tableA = new PriceTable();
                tableA.setTableName("A");
                processArchiveFile(archive, tableA).save(em);
            }
            archive = new File("files/import/ArchiveB-"+DateTime.now().getYear()+".csv");
            if(archive.exists())
            {
                PriceTable tableB = new PriceTable();
                tableB.setTableName("B");
                processArchiveFile(archive, tableB).save(em);
            }
            em.getTransaction().commit();
            Global.app.connectDataSources();
            return;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
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

    /**
     * Executed after first run of program, fills DB with archived data from the past.
     * @param archive archive file in .csv format, downloaded from npb.pl
     * @param table table to which newly created data will be saved.
     * @return table filled with data, ready to be saved.
     */
    private static PriceTable processArchiveFile(File archive, PriceTable table)
    {
        try
        {
            InputStream inputStream = new FileInputStream(archive);
            Reader in = new InputStreamReader(inputStream, "Windows-1250");
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').parse(in);
            int i = 0;
            Map<Integer, Currency> csvCurrencyMap = new HashMap<>();                                                    // Map containing currency and corresponding column in csv file
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");


            for(CSVRecord record : records)
            {
                int j;
                if(i == 0)                                                                                              // First line is a header containing converter and currency code
                {
                    j = 1;
                    while(j < record.size() - 3)
                    {
                        String currencyString = record.get(j);
                        if(currencyString == null || currencyString.isEmpty())
                            break;

                        String[] splittedString = currencyString.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        Currency newCurrency = new Currency();
                        newCurrency.setConverter(Double.parseDouble(splittedString[0]));
                        newCurrency.setCurrencyCode(splittedString[1]);
                        table.getCurrencies().add(newCurrency);
                        csvCurrencyMap.put(j, newCurrency);
                        j++;
                    }
                }
                else if(i == 1)                                                                                         // Second line is a header containing currency name
                {
                    j = 1;
                    while(true)
                    {
                        String currencyName = record.get(j);
                        if(currencyName == null || currencyName.isEmpty())
                            break;
                        Currency matchedCurrency = csvCurrencyMap.get(j);
                        matchedCurrency.setName(currencyName);
                        j++;
                    }
                }
                else
                {
                    if(!record.get(0).startsWith(""+DateTime.now().getYear()))                                          // It means we reached document's footer
                        break;
                    j = 1;
                    DateTime publicationDate = formatter.parseDateTime(record.get(0));
                    String plainDate = publicationDate.getYear() + "-" + publicationDate.getMonthOfYear() + "-" + publicationDate.getDayOfMonth();
                    table.setPublicationDate(plainDate);
                    while(j < record.size() - 3)
                    {
                        String currencyPriceString = record.get(j);
                        if(currencyPriceString == null || currencyPriceString.isEmpty())
                            break;
                        Currency matchedCurrency = csvCurrencyMap.get(j);
                        CurrencyPrice currencyPrice = new CurrencyPrice(currencyPriceString);
                        currencyPrice.setDate(publicationDate);
                        matchedCurrency.getAvgPrices().add(currencyPrice);
                        j++;
                    }

                }
                i++;
            }
        }
        catch (FileNotFoundException e)
        {
            logger.error("File not found!", e);
        }
        catch (IOException e)
        {
            logger.error("Error while processing archive file!", e);
        }
        return table;
    }
}
