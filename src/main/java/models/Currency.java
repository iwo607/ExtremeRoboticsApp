package models;

import common.models.AbstractModel;
import models.xml.XMLCurrency;

import java.util.Comparator;
import java.util.HashSet;
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

    public CurrencyPrice getCurrentPrice()
    {
        if(avgPrices.size() > 0)
            return avgPrices.stream().sorted(Comparator.comparing(CurrencyPrice::getDate)).findFirst().get();
        return null;
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
