package models.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Object, that represents single currency downloaded from nbp.pl in .xml format
 */
public class XMLCurrency
{
    private Long id;
    private String name;
    private String converter;
    private String currencyCode;
    private String avgPrice;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlElement(name="nazwa_waluty")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name="przelicznik")
    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }

    @XmlElement(name="kod_waluty")
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @XmlElement(name="kurs_sredni")
    public String getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(String avgPrice) {
        this.avgPrice = avgPrice;
    }

}
