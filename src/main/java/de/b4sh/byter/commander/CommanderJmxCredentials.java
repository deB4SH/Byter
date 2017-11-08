package de.b4sh.byter.commander;

import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;

import de.b4sh.byter.utils.jmx.JmxEntity;

/**
 * Class for credentials used to connect to other jmx instances.
 */
@JMXBean(description = "Byter.Commander.Credentials")
public final class CommanderJmxCredentials extends JmxEntity {
    private static final Logger log = Logger.getLogger(CommanderJmxCredentials.class.getName());
    private String username;
    private String password;
    private String hostName;
    private int hostPort;

    /**
     * public constructor with base initialisation.
     * @param packageName set the packagename this controller should be available under
     * @param type set the type this controller should be available under
     */
    public CommanderJmxCredentials(final String packageName, final String type) {
        super(packageName, type);
        this.hostName = "null";
        this.hostPort = -1;
        this.username = "null";
        this.password = "null";
    }

    /**
     * get the current set user name.
     * @return String
     */
    String getUsername() {
        return username;
    }

    /**
     * set a new user name.
     * @param username user name to set
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * get the current set password.
     * @return String
     */
    String getPassword() {
        return password;
    }

    /**
     * set a new password.
     * @param password password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * get the current set host name.
     * @return String
     */
    String getHostName() {
        return hostName;
    }

    /**
     * set a new host name.
     * @param hostName host name to set
     */
    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    /**
     * get the current set host port.
     * @return int
     */
    public int getHostPort() {
        return hostPort;
    }

    /**
     * set a new port.
     * @param hostPort port to set
     */
    public void setHostPort(final int hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * get the user name via jmx.
     * @return String
     */
    @JMXBeanAttribute(name = "username", description = "username to login on jmx component")
    public String getUsernameViaJmx(){
        return getUsername();
    }

    /**
     * get the password via jmx.
     * @return String
     */
    @JMXBeanAttribute(name = "password", description = "password to login on jmx component")
    public String getPasswordViaJmx(){
        return getPassword();
    }

    /**
     * get the host name via jmx.
     * @return String
     */
    @JMXBeanAttribute(name = "hostname", description = "hostname of the other jmx component")
    public String getHostNameViaJmx(){
        return getHostName();
    }

    /**
     * get the host port via jmx.
     * @return String
     */
    @JMXBeanAttribute(name = "hostport", description = "port of the other jmx component")
    public int getHostPortViaJmx(){
        return getHostPort();
    }
}
