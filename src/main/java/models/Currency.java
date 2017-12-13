package models;

import common.models.AbstractModel;
import models.xml.XMLCurrency;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Object, that represents currency after processing it from xml
 */
public class Currency extends AbstractModel
{

    private Long id;

    private String name;
    private String currencyCode;

    private Double converter;

    private Set<CurrencyPrice> avgPrices = new HashSet<>();

    public Currency(){}

    /**
     * Creates new object from its xml equivalent
     */
    public Currency(XMLCurrency xmlCurrency)
    {
        setName(xmlCurrency.getName());
        setCurrencyCode(xmlCurrency.getCurrencyCode());
        setConverter(Double.parseDouble(xmlCurrency.getConverter()));
    }

    //TODO utworzyć repozytorium
    //TODO zastąpić criteria builderem

    /**
     * Used to get all currencies from DB
     * @param em Entity Manager, that will be used to connect to DB
     * @return list of currencies
     */
    @SuppressWarnings("unchecked")
    public static List<Currency> getAllCurrencies(EntityManager em)
    {
        Session session = em.unwrap(Session.class);
        Criteria criteria = session.createCriteria(Currency.class);
        criteria.addOrder(Order.asc("currencyCode"));
        return criteria.list();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getConverter() {
        return converter;
    }

    public void setConverter(Double converter) {
        this.converter = converter;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Set<CurrencyPrice> getAvgPrices() {
        return avgPrices;
    }

    public void setAvgPrices(Set<CurrencyPrice> avgPrices) {
        this.avgPrices = avgPrices;
    }

}
