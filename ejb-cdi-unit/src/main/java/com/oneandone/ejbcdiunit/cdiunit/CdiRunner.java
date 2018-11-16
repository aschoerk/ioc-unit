package com.oneandone.ejbcdiunit.cdiunit;
/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;

import org.jboss.weld.transaction.spi.TransactionServices;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.oneandone.cdi.weldstarter.WeldSetup;
import com.oneandone.cdi.weldstarter.WeldSetupClass;
import com.oneandone.cdi.weldstarter.spi.WeldStarter;
import com.oneandone.ejbcdiunit.CdiTestConfig;
import com.oneandone.ejbcdiunit.CreationalContexts;
import com.oneandone.ejbcdiunit.EjbUnitTransactionServices;
import com.oneandone.ejbcdiunit.SupportEjbExtended;
import com.oneandone.ejbcdiunit.cfganalyzer.TestConfigAnalyzer;
import com.oneandone.ejbcdiunit.internal.EjbInformationBean;

/**
 * <code>&#064;CdiRunner</code> is a JUnit runner that uses a CDI container to
 * create unit test objects. Simply add
 * <code>&#064;RunWith(CdiRunner.class)</code> to your test class.
 *
 * <pre>
 * <code>
 * &#064;RunWith(CdiRunner.class) // Runs the test with CDI-Unit
 * class MyTest {
 *   &#064;Inject
 *   Something something; // This will be injected before the tests are run!
 *
 *   ... //The rest of the test goes here.
 * }</code>
 * </pre>
 *
 * @author Bryn Cooke
 */
public class CdiRunner extends BlockJUnit4ClassRunner {

    private static final String ABSENT_CODE_PREFIX = "Absent Code attribute in method that is not native or abstract in class file ";
    protected Throwable startupException;
    protected FrameworkMethod frameworkMethod;
    private Class<?> clazz;
    protected WeldStarter weldStarter;
    private WeldSetupClass weldSetup;

    public CdiRunner(Class<?> clazz) throws InitializationError {
        super(checkClass(clazz));
        this.clazz = clazz;
    }

    private static Class<?> checkClass(Class<?> clazzP) {
        try {
            for (Method m : clazzP.getMethods()) {
                m.getReturnType();
                m.getParameterTypes();
                m.getParameterAnnotations();
            }
            for (Field f : clazzP.getFields()) {
                f.getType();
            }
        } catch (ClassFormatError e) {
            throw parseClassFormatError(e);
        }
        return clazzP;
    }

    private static ClassFormatError parseClassFormatError(ClassFormatError e) {
        if (e.getMessage().startsWith(ABSENT_CODE_PREFIX)) {
            String offendingClass = e.getMessage().substring(ABSENT_CODE_PREFIX.length());
            URL url = CdiRunner.class.getClassLoader().getResource(offendingClass + ".class");

            return new ClassFormatError("'" + offendingClass.replace('/', '.')
                    + "' is an API only class. You need to remove '"
                    + url.toString().substring(9, url.toString().indexOf("!")) + "' from your classpath");
        } else {
            return e;
        }
    }

    protected Object createTest() throws Exception {
        try {
            weldStarter = WeldSetupClass.getWeldStarter();
            String version = weldStarter.getVersion();
            if ("2.2.8 (Final)".equals(version) || "2.2.7 (Final)".equals(version)) {
                startupException = new Exception("Weld 2.2.8 and 2.2.7 are not supported. Suggest upgrading to 2.2.9");
            }

            final CdiTestConfig weldTestConfig =
                    new CdiTestConfig(clazz, frameworkMethod.getMethod(), weldStarter)
                            .addClass(SupportEjbExtended.class)
                            .addServiceConfig(new CdiTestConfig.ServiceConfig(TransactionServices.class, new EjbUnitTransactionServices()))
            ;


            try {
                if (weldSetup == null) {
                    TestConfigAnalyzer cdiUnitAnalyzer = new TestConfigAnalyzer();
                    cdiUnitAnalyzer.analyze(weldTestConfig);
                    weldSetup = new WeldSetupClass();
                    weldSetup.setBeanClassNames(weldTestConfig.getDiscoveredClasses());

                    weldSetup.setAlternativeClasses(weldTestConfig.getAlternatives());
                    weldSetup.setEnabledAlternativeStereotypeNames(weldTestConfig.getEnabledAlternativeStereotypes());
                    weldSetup.setEnabledDecorators(weldTestConfig.getEnabledDecorators());
                    weldSetup.setEnabledInterceptors(weldTestConfig.getEnabledInterceptors());

                    weldSetup.setExtensionObjects(weldTestConfig.getExtensions());
                    weldSetup.addService(new WeldSetup.ServiceConfig(TransactionServices.class, new EjbUnitTransactionServices()));
                }
                weldStarter.start(weldSetup);

                System.setProperty("java.naming.factory.initial", "com.oneandone.cdiunit.internal.naming.CdiUnitContextFactory");

                InitialContext initialContext = new InitialContext();
                final BeanManager beanManager = weldStarter.get(BeanManager.class);
                initialContext.bind("java:comp/BeanManager", beanManager);
                try (CreationalContexts creationalContexts = new CreationalContexts(beanManager)) {
                    EjbInformationBean ejbInformationBean =
                            (EjbInformationBean) creationalContexts.create(EjbInformationBean.class, ApplicationScoped.class);
                    ejbInformationBean.setApplicationExceptionDescriptions(weldTestConfig.getApplicationExceptionDescriptions());
                } finally {
                    initialContext.close();
                }
            } catch (Throwable e) {
                if (startupException == null) {
                    startupException = e;
                }
                if (e instanceof ClassFormatError) {
                    throw e;
                }
            }

        } catch (ClassFormatError e) {

            startupException = parseClassFormatError(e);
        } catch (Throwable e) {
            startupException = new Exception("Unable to start weld", e);
        }

        return createTest(clazz);
    }

    private <T> T createTest(Class<T> testClass) {

        T t = weldStarter.get(testClass);

        return t;
    }
}
