package de.b4sh.byter.example.staticmbean;

/**
 * Static Interface for Showcase Class ExampleStatic.
 * required to be public for reflections on the mbean server
 * @see de.b4sh.byter.example.staticmbean.ExampleStatic
 */
public interface ExampleStaticMBean {
    //operations

    /**
     * Function for printing hello world on console.
     * @param name name to print
     */
    void printHelloWorld(String name);

    /**
     * Function to increment a value by given key.
     * @param incValue key to increment
     */
    void incValue(int incValue);
    //getter

    /**
     * Get for Hello.
     * @return String
     */
    String getHello();

    /**
     * Get for Value.
     * @return int
     */
    int getValue();
    //setter

    /**
     * Set for Hello.
     * @param hello String
     */
    void setHello(String hello);
}
