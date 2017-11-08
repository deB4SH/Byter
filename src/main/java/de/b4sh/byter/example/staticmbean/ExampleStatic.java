package de.b4sh.byter.example.staticmbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Showcase class for static mbean implementation.
 * Implements ExampleStaticMBean and does magic!
 */
public final class ExampleStatic implements ExampleStaticMBean{
    private final ObjectName objectName;
    private String hello;
    private int value;

    /**
     * Constructor for ExampleStatic.
     * @param hello a parameter
     * @param value an another parameter
     * @throws MalformedObjectNameException thrown due to malformed objectName
     *                                      -> this should not happen here :)
     */
    public ExampleStatic(final String hello, final int value) throws MalformedObjectNameException {
        this.hello = hello;
        this.value = value;
        this.objectName = new ObjectName( "de.b4sh.byter.example:type=StaticMBeanExample");
    }

    /**
     * Function for JMX to issue a hello world.
     * @param name Name to print out
     */
    @Override
    public void printHelloWorld(final String name) {
        System.out.println("Hello World at you " + name);
    }

    /**
     * Function for JMX to issue an increment on value x.
     * @param incValue increment value by
     */
    @Override
    public void incValue(final int incValue) {
        this.value += incValue;
    }

    /**
     * Get for hello.
     * @return String with value of hello
     */
    @Override
    public String getHello() {
        return this.hello;
    }

    /**
     * Get for value.
     * @return Int with value of value
     */
    @Override
    public int getValue() {
        return this.value;
    }

    /**
     * Set for hello.
     * @param hello String with new value for hello
     */
    @Override
    public void setHello(final String hello) {
        this.hello = hello;
    }

    /**
     * Get for value of ObjectName.
     * @return value of object name
     */
    public ObjectName getObjectName(){
        return this.objectName;
    }
}
