package models;

import common.models.AbstractModel;
import org.joda.time.DateTime;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Object, that keeps information about currency price for specific day
 */
public class CurrencyPrice extends AbstractModel
{
    private Long id;
    private Float price;
    private DateTime date;

    public CurrencyPrice(){}

    public CurrencyPrice(String price)
    {
        setPrice(Float.parseFloat(price.replaceAll(",", ".")));
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }
}
