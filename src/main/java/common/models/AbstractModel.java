package common.models;

import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractModel
{

    public abstract Long getId();

    public void save(EntityManager em) throws ConstraintViolationException
    {
        if(getId() == null || getId() == 0)
            em.persist(this);
        else
            em.merge(this);
    }

    public void remove(EntityManager em) {
        em.remove(this);
    }
}
