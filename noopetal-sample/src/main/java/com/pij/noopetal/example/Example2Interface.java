package com.pij.noopetal.example;

import com.pij.noopetal.Noop;

/**
 * @author Pierrejean on 23/10/2015.
 */
@Noop("test.HaExample2Interface")
public interface Example2Interface {

    void doSomething(String arg, int theInt, Long longObject);

    Long getSomething();
}
