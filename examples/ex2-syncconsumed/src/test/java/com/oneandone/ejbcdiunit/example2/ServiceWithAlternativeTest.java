package com.oneandone.ejbcdiunit.example2;

import com.oneandone.cdi.testanalyzer.annotations.SutPackages;
import com.oneandone.cdi.tester.CdiUnit2Runner;
import com.oneandone.cdi.testanalyzer.annotations.ProducesAlternative;
import com.oneandone.ejbcdiunit.example2.uselookup.Resources;
import com.oneandone.ejbcdiunit.example2.uselookup.ServiceWithLookup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * @author aschoerk
 */
@RunWith(CdiUnit2Runner.class)
@SutPackages({ServiceWithLookup.class})
public class ServiceWithAlternativeTest {
    @Inject
    ServiceIntf sut;

    @Mock
    RemoteServiceIntf remoteService;

    @ProducesAlternative
    @Produces
    @Mock
    Resources resources;

    long idGenerator = 0;

    @Before
    public void beforeTestService() {
        when(resources.lookupRemoteService()).thenReturn(remoteService);
        when(remoteService.newEntity1(anyInt(), anyString())).thenReturn(++idGenerator);
    }

    @Test
    public void canServiceInsertEntity1Remotely() {
        long id = sut.newRemoteEntity1(1, "test1");
        assertThat(id, is(1L));
    }

    @Test
    public void canReadEntity1AfterInsertion() {
        when(remoteService.getStringValueFor(anyLong())).thenReturn("something");
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(sut.newRemoteEntity1(i, "string: " + i));
        }
        // fetch the 6th inserted entity.
        assertThat(sut.getRemoteStringValueFor(ids.get(5)), is(remoteService.getStringValueFor(ids.get(5))));
    }

}
