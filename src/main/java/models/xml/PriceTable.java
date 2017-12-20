package models.xml;

import common.models.AbstractModel;
import org.hibernate.Criteria;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Object, that represents table of currency prices downloaded from nbp.pl in .xml format
 */
@XmlRootElement(name="tabela_kursow")
public class PriceTable extends AbstractModel
{
    private Long id;
    private String tableName;
    private String tableNr;
    private String publicationDate;
    private List<XMLCurrency> xmlCurrencies;
    private Set<models.Currency> currencies = new HashSet<>();

    /**
     * Gets all price tables from DB
     * @param em Entity Manager used while connecting with DB
     * @return list of price tables
     */
    @SuppressWarnings("unchecked")
    public static List<PriceTable> getAllTables(EntityManager em)
    {
        Session session = em.unwrap(Session.class);
        Criteria criteria = session.createCriteria(PriceTable.class);
        return criteria.list();
    }

    public Optional<models.Currency> getCurrencyByName(String name)
    {
        return currencies.stream().filter(currency -> currency.getName().equals(name)).findFirst();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlElement(name="numer_tabeli")
    public String getTableNr()
    {
        return tableNr;
    }

    public void setTableNr(String tableNr)
    {
        this.tableNr = tableNr;
    }

    @XmlElement(name="data_publikacji")
    public String getPublicationDate()
    {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate)
    {
        this.publicationDate = publicationDate;
    }

    @XmlElement(name="pozycja")
    public List<XMLCurrency> getXmlCurrencies() {
        return xmlCurrencies;
    }

    public void setXmlCurrencies(List<XMLCurrency> currencies) {
        this.xmlCurrencies = currencies;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Set<models.Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Set<models.Currency> currencies) {
        this.currencies = currencies;
    }
}
