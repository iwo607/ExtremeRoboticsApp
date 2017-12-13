package services;

import models.Currency;
import models.xml.PriceTable;
import java.util.List;

/**
 * Created by Iwo Skwierawski on 13.12.17.
 * This services handles connections to DB related to currencies
 */
public interface CurrencyService
{
    /**
     * Gets all price tables from DB
     * @return list of price tables
     */
    List<PriceTable> getAllTables();

    /**
     * Used to get all currencies from DB
     * @return list of currencies
     */
    List<Currency> getAllCurrencies();
}
