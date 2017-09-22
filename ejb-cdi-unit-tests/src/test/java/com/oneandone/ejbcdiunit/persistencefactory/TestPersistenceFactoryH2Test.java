package com.oneandone.ejbcdiunit.persistencefactory;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.oneandone.ejbcdiunit.EjbUnitRunner;
import com.oneandone.ejbcdiunit.entities.TestEntity1;
import com.oneandone.ejbcdiunit.persistence.TestPersistenceFactory;

/**
 * @author aschoerk
 */
@RunWith(EjbUnitRunner.class)
@AdditionalClasses({ TestPersistenceFactory.class, TestEntity1.class })
public class TestPersistenceFactoryH2Test extends PersistenceFactoryTestBase {
    @Before
    public void beforeTestPersistenceFactoryH2Test() {
        System.setProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
    }

    @Override
    public void doesFlushBeforeNativeQuery() throws Exception {

    }
}
