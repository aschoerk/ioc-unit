package com.oneandone.iocunitejb.cdibeans;

/**
 * @author aschoerk
 */
public class ServiceBeanAppScopedHelper {

    int initCalled;

    public void callInit() {
        System.out.println("callInit " + this.getClass().getSimpleName());
        initCalled++;
    }
}
