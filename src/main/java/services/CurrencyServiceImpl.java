package services;

import common.repository.HibernateRepository;
import common.repository.Repository;
import models.Currency;
import models.CurrencyPrice;
import models.xml.PriceTable;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Iwo Skwierawski on 13.12.17.
 * Implements methods from FPS interface
 */
public class CurrencyServiceImpl implements CurrencyService
{
    private final Repository<PriceTable> tableReo;
    private final Repository<Currency> currencyRepo;
    private final Repository<CurrencyPrice> priceRepo;

    public CurrencyServiceImpl(EntityManager em)
    {
        this.tableReo = new HibernateRepository<>(PriceTable.class);
        tableReo.setEm(em);
        this.currencyRepo = new HibernateRepository<>(Currency.class);
        currencyRepo.setEm(em);
        this.priceRepo = new HibernateRepository<>(CurrencyPrice.class);
        priceRepo.setEm(em);
    }

    @Override
    public List<PriceTable> getAllTables()
    {
        return tableReo.findAll();
    }

    @Override
    public List<Currency> getAllCurrencies()
    {
        return currencyRepo.findAll();
    }
}
