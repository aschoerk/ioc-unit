package com.oneandone.iocunit.analyzer.specializing.testsamples;

import javax.inject.Inject;

import com.oneandone.iocunit.analyzer.annotations.SutClasspaths;
import com.oneandone.iocunit.analyzer.specializing.sutsamples.BaseClass;

/**
 * @author aschoerk
 */
@SutClasspaths(BaseClass.class)
public class TestWithBaseAsSutClasspathBaseAsInject {
    @Inject
    BaseClass baseClass;
}
