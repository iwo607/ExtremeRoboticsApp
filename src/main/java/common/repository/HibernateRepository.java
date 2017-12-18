package common.repository;

import com.google.common.collect.ImmutableMap;
import common.models.AbstractModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class HibernateRepository<T extends AbstractModel> implements Repository<T> {

    private EntityManager em;
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public HibernateRepository(Class type) {
        this.type = type;
    }

    private Session getSession() {
        Session session = checkEM().unwrap(Session.class);
        if(!session.isOpen() || !session.isConnected())
        {
            session = session.getSessionFactory().openSession();
            session.beginTransaction();
        }
        return session;
    }

    private Criteria getCriteria()
    {
        return getSession().createCriteria(type);
    }

    @Override
    public void delete(Long id)
    {
        T obj = checkEM().find(type, id);
        if(obj != null) {
            em.remove(obj);
        }
    }

    private synchronized EntityManager checkEM()                                                                        // Przed każdą transakcją z EM należy go ustawić, gdyż wraca automatycznie do wartości domyślnej
    {
        if(em == null)
        {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit");
            em = emf.createEntityManager();
        }
        return em;
    }

    @Override
    public List<T> findAll(String... aliases) {
        Map<String, JoinType> map = new HashMap<>();
        Stream.of(aliases).forEach(alias -> map.put(alias, JoinType.LEFT_OUTER_JOIN));
        return findAll(map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll(Map<String, JoinType> aliases) {
        Criteria criteria = getCriteria();
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        addAliases(criteria, aliases);
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAllOrdered(Order order, String... aliases)
    {
        Criteria criteria = getCriteria();
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        addAliases(criteria, aliases);
        criteria.addOrder(order);
        return criteria.list();
    }

    @Override
    public List<T> findWithRestrictions(List<?> restrictions, String... aliases) {
        Map<String, JoinType> map = new HashMap<>();
        Stream.of(aliases).forEach(alias -> map.put(alias, JoinType.LEFT_OUTER_JOIN));
        return findWithRestrictions(restrictions, map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findWithRestrictions(List<?> restrictions, Map<String, JoinType> aliases) {
        Criteria criteria = getCriteria();

        addRestrictions(criteria, restrictions);
        addAliases(criteria, aliases);

        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        return criteria.list();
    }

    @Override
    public T findById(Long id) {
        return checkEM().find(type, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findOneByField(String field, Object value, String... aliases) {
        return this.findOneByFields(ImmutableMap.of(field, value), aliases);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findOneByField(String field, Object value, Boolean ignoreCase, String... aliases) {
        return this.findOneByFields(ImmutableMap.of(field, value), ignoreCase, aliases);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findOneByFields(Map<String, ?> restrictions, String... aliases) {
        return findOneByFields(restrictions, false, aliases);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findOneByFields(Map<String, ?> restrictions, Boolean ignoreCase, String... aliases) {
        Criteria criteria = getCriteria();
        if(!ignoreCase)
            restrictions.forEach((key, value) -> criteria.add(Restrictions.eq(key, value)));
        else
        {
            restrictions.forEach((key, value) ->
            {
                if(value instanceof String)
                    criteria.add(Restrictions.eq(key, value).ignoreCase());
                else
                    criteria.add(Restrictions.eq(key, value));
            });
        }
        criteria.setMaxResults(1);


        addAliases(criteria, aliases);

        Object result = criteria.uniqueResult();
        return result == null ? null : (T) result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T findOneWithRestrictions(List<?> restrictions, String... aliases) {
        Criteria criteria = getCriteria();

        addRestrictions(criteria, restrictions);
        addAliases(criteria, aliases);

        criteria.setMaxResults(1);

        Object result = criteria.uniqueResult();
        return result == null ? null : (T) result;
    }

    @Override
    public List<T> findByField(String field, Object value, String... aliases) {
        return this.findByFields(ImmutableMap.of(field, value), aliases);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findByFields(Map<String, ?> restrictions, String... aliases) {
        Criteria criteria = getCriteria();
        restrictions.forEach((key, value) -> criteria.add(Restrictions.eq(key, value)));

        addAliases(criteria, aliases);

        return criteria.list();
    }

    @Override
    public Boolean checkExistencyByField(String field, Object value, String... aliases)
    {
        return findByField(field, value, aliases).size() > 0;
    }

    @Override
    public Boolean checkExistencyByFields(Map<String, ?> restrictions, String... aliases)
    {
        return findByFields(restrictions, aliases).size() > 0;
    }

    @Override
    public DetachedCriteria createDetachedCriteria(Class clazz, List<Criterion> restrictions, List<?> projections, String... aliases)
    {
        DetachedCriteria innerCriteria = DetachedCriteria.forClass(clazz);
        addRestrictions(innerCriteria, restrictions);
        addAliases(innerCriteria, aliases);
        addProjections(innerCriteria, projections);
        return innerCriteria;
    }

    @Override
    public Criteria createComplexQuery(List<?> restrictions, String... aliases)
    {
        Criteria criteria = getCriteria();
        addRestrictions(criteria, restrictions);
        addAliases(criteria, aliases);
        return criteria;
    }

    @Override
    public Criteria createComplexQuery(List<?> restrictions, Map<String, DetachedCriteria> dcMap, String... aliases)
    {
        Criteria criteria = createComplexQuery(restrictions, aliases);
        addDetachedCriteria(criteria, dcMap);
        return criteria;
    }

    @Override
    public Criteria createComplexQuery(List<?> restrictions, List<?> projections, String... aliases)
    {
        Criteria criteria = createComplexQuery(restrictions, aliases);
        addProjections(criteria, projections);
        return criteria;
    }

    @Override
    public Criteria createComplexQuery(List<?> restrictions, List<?> projections, Map<String, DetachedCriteria> dcMap, String... aliases)
    {
        Criteria criteria = createComplexQuery(restrictions, dcMap, aliases);
        addProjections(criteria, projections);
        return criteria;
    }

    private void addRestrictions(Criteria criteria, List<?> restrictions) {
        if(restrictions == null) return;
        restrictions.stream()
                .filter(restriction -> restriction instanceof Criterion)
                .forEach(restriction -> criteria.add((Criterion) restriction));
    }

    private void addRestrictions(DetachedCriteria criteria, List<?> restrictions) {
        if(restrictions == null) return;
        restrictions.stream()
                .filter(restriction -> restriction instanceof Criterion)
                .forEach(restriction -> criteria.add((Criterion) restriction));
    }

    private void addProjections(Criteria criteria, List<?> projections)
    {
        if(projections == null) return;
        ProjectionList projectionList = Projections.projectionList();
        projections.stream()
                .filter(projection -> projection instanceof String || projection instanceof Projection)
                .forEach(projection -> {
                    if(projection instanceof String)
                        projectionList.add(Projections.distinct(Projections.property((String)projection)));
                    else
                        projectionList.add((Projection) projection);

                });
        criteria.setProjection(projectionList);
    }

    private void addProjections(DetachedCriteria criteria, List<?> projections)
    {
        if(projections == null) return;
        ProjectionList projectionList = Projections.projectionList();
        projections.stream()
                .filter(projection -> projection instanceof String || projection instanceof Projection)
                .forEach(projection -> {
                    if(projection instanceof String)
                        projectionList.add(Projections.distinct(Projections.property((String)projection)));
                    else
                        projectionList.add((Projection) projection);

                });
        criteria.setProjection(projectionList);
    }

    private void addAliases(Criteria criteria, Map<String, JoinType> aliases) {
        if (aliases == null) return;
        aliases.forEach((k, v) -> {
            // If the alias contains a dot use the full alias as the left side and the part after the dot as the
            // right side, this lets you join multiple tables with references to other tables
            // eg. table.currencies.prices
            if (k.contains(".")) {
                String rhs = k.split("\\.")[1];
                criteria.createAlias(k, rhs, v);
            } else criteria.createAlias(k, k, v);
        });
    }

    private void addAliases(Criteria criteria, String... aliases) {
        if (aliases == null) return;
        Arrays.stream(aliases).forEach(alias -> {
            if (alias.contains(".")) {
                String rhs = alias.split("\\.")[1];
                criteria.createAlias(alias, rhs, JoinType.LEFT_OUTER_JOIN);
            } else criteria.createAlias(alias, alias, JoinType.LEFT_OUTER_JOIN);
        });
    }

    private void addAliases(DetachedCriteria criteria, String... aliases)
    {
        if (aliases == null) return;
        Arrays.stream(aliases).forEach(alias -> {
            if (alias.contains(".")) {
                String rhs = alias.split("\\.")[1];
                criteria.createAlias(alias, rhs, JoinType.LEFT_OUTER_JOIN);
            } else criteria.createAlias(alias, alias, JoinType.LEFT_OUTER_JOIN);
        });
    }

    private void addDetachedCriteria(Criteria criteria, Map<String, DetachedCriteria> dcMap)
    {
        if(dcMap == null) return;
        dcMap.forEach((k, v) -> {
            if(k != null && v != null)
                criteria.add(Subqueries.propertyIn(k, v));
        });
    }

    public synchronized void setEm(EntityManager em) {
        this.em = em;
    }
}
