package common.repository;

import common.models.AbstractModel;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinType;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * Created by Iwo Skwierawski on 13.12.17.
 * Contains methods that are commonly used while connecting to DB
 */
public interface Repository<T extends AbstractModel>
{
    /**
     * Deletes object by its id
     * @param id id of object to delete
     */
    void delete(Long id);

    /**
     * Finds object by its id
     * @param id id of object to delete
     * @return found object
     */
    T findById(Long id);

    /**
     * Finds object by one of its fields
     * @param field that is used to search
     * @param value of specified field
     * @param ignoreCase should case be ignored? DEFAULT = FALSE
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return found object
     */
    T findOneByField(String field, Object value, Boolean ignoreCase, String... aliases);
    T findOneByField(String field, Object value, String... aliases);

    /**
     * Finds object by multiple fields
     * Case sensitive in default
     * @param restrictions map, containing K: field, V: value of that field
     * @param ignoreCase should case be ignored? DEFAULT = FALSE
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return found object
     */
    T findOneByFields(Map<String, ?> restrictions, Boolean ignoreCase, String... aliases);
    T findOneByFields(Map<String, ?> restrictions, String... aliases);

    /**
     * Finds object by predefined restrictions
     * @param restrictions list of restrictions
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return found object
     */
    T findOneWithRestrictions(List<?> restrictions, String... aliases);

    /**
     * Finds all objects containing exact value in specified field
     * @param field that is used to search
     * @param value of specified field
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return list of objects matching restrictions
     */
    List<T> findByField(String field, Object value, String... aliases);

    /**
     * Finds all objects matching values exactly, in specified fields
     * @param restrictions map, containing K: field, V: value of that field
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return list of objects matching restrictions
     */
    List<T> findByFields(Map<String, ?> restrictions, String... aliases);

    /**
     * Finds all objects matching predefined restrictions
     * @param restrictions list of restrictions
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return list of object matching restrictions
     */
    List<T> findWithRestrictions(List<?> restrictions, Map<String, JoinType> aliases);
    List<T> findWithRestrictions(List<?> restrictions, String... aliases);

    /**
     * Returns list of all objects from that class.
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return list of all object from that class
     */
    List<T> findAll(String... aliases);
    List<T> findAll(Map<String, JoinType> aliases);

    List<T> findAllOrdered(Order order, String... aliases);

    /**
     * Checks if object containing exact field value is already present in DB
     * @param field that is used to search
     * @param value of specified field
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return found match? T/F
     */
    Boolean checkExistencyByField(String field, Object value, String... aliases);

    /**
     * Checks if object containing exact fields values is already present in DB
     * @param restrictions map, containing K: field, V: value of that field
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return found match? T/F
     */
    Boolean checkExistencyByFields(Map<String, ?> restrictions, String... aliases);

    /**
     * Creates detached criteria for specific class, matching predefined restrictions
     * @param clazz root class
     * @param restrictions list of predefined restrictions
     * @param projections property, that will be returned from query for later use in propertyIn for primary Criteria (projections may be either Projection or String(for property projections))
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return created detached criteria
     */
    DetachedCriteria createDetachedCriteria(Class clazz, List<Criterion> restrictions, List<?> projections, String... aliases);

    /**
     * Creates complex query from provided data, ready to be executed.
     * @param restrictions list of predefined restrictions
     * @param projections projections for result (projections may be either Projection or String(for property projections))
     * @param dcMap map of DetachedCriteria, where K: field for propertyIn parameter, V: created DetachedCriteria
     * @param aliases tables to join DEFAULT = LEFT OUTER JOIN
     * @return criteria created from provided data, ready to be executed
     */
    Criteria createComplexQuery(List<?> restrictions, List<?> projections, Map<String, DetachedCriteria> dcMap, String... aliases);
    Criteria createComplexQuery(List<?> restrictions, String... aliases);
    Criteria createComplexQuery(List<?> restrictions, Map<String, DetachedCriteria> dcMap, String... aliases);
    Criteria createComplexQuery(List<?> restrictions, List<?> projections, String... aliases);

    /**
     * Sets EntityManager for this repository, which will be used in all connections with DB
     * @param em EntityManager
     */
    void setEm(EntityManager em);
}
