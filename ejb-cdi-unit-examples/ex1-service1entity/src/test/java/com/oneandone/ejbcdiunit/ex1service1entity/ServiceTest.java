package com.oneandone.ejbcdiunit.ex1service1entity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.oneandone.ejbcdiunit.EjbUnitRunner;
import com.oneandone.ejbcdiunit.persistence.TestPersistenceFactory;

/**
 * @author aschoerk
 */
@RunWith(EjbUnitRunner.class)
@AdditionalClasses({Service.class, TestPersistenceFactory.class})
public class ServiceTest {
    @Inject
    ServiceIntf sut;

    @Inject
    EntityManager entityManager;

    @Test
    public void canServiceReturnFive() {
        assertThat(sut.returnFive(), is(5));
    }

    @Test
    public void canServiceInsertEntity1() {
        long id = sut.newEntity1(1, "test1");
        assertThat(id, is(1L));
        List<Entity1> resultList = entityManager.createQuery("Select e from Entity1 e", Entity1.class).getResultList();
        assertThat(resultList.size(), is(1));
        Entity1 entity1 = resultList.iterator().next();
        assertThat(entity1.getIntValue(), is(1));
        assertThat(entity1.getStringValue(), is("test1"));
    }

    @Test
    public void canReadEntity1AfterInsertion() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(sut.newEntity1(i, "string: " + i));
        }
        // fetch the 6th inserted entity.
        assertThat(sut.getStringValueFor(ids.get(5)), is("string: 5"));
    }
}
