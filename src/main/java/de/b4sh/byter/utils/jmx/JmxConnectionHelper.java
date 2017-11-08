package de.b4sh.byter.utils.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import de.b4sh.byter.utils.io.PortScanner;

/**
 * HelperClass for connecting to different JMX-Transport implementation.
 * 2017-06-13: jmxmp support
 */
public final class JmxConnectionHelper {

    private static final Logger log = Logger.getLogger(JmxConnectionHelper.class.getName());

    private JmxConnectionHelper(){
        //nop
    }

    /**
     * Shorthand jmxConnector Builder.
     * @param host host address
     * @param port port of the host
     * @return connected JMXConnector
     * @throws IOException IOException
     */
    public static JMXConnector buildJmxMPConnector(final String host, final int port) throws IOException {
        return buildJmxMPConnector(host,port,null,null);
    }

    /**
     * Creates the jmxConnector with passed parameters.
     * @param host host address
     * @param port port of the host
     * @param user username (null possible)
     * @param pass password (null possible)
     * @return connected JMXConnector
     * @throws IOException IOException
     */
    public static JMXConnector buildJmxMPConnector(final String host, final int port, final String user, final String pass) throws IOException {
        try {
            final JMXServiceURL serviceURL = new JMXServiceURL("jmxmp", host,port);
            if("null".equals(user) || "null".equals(pass) || user == null || pass == null){
                return JMXConnectorFactory.connect(serviceURL);
            }
            final Map<String, Object> environment = new HashMap<>();
            environment.put("jmx.remote.credentials", new String[]{user,pass});
            environment.put(Context.SECURITY_PRINCIPAL, user);
            environment.put(Context.SECURITY_CREDENTIALS, pass);
            return JMXConnectorFactory.connect(serviceURL,environment);
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "Malformed ServiceURL in buildJmxConnector");
            return null;
        }
    }

    /**
     * Create a JMXMP Connector.
     * @param startPort startport to look for an open port
     * @param maxRange maximum port scan range
     * @param mbs desired mbean server to register to
     * @return JmxConnectorServer that could be started
     * @throws IOException io exception if something bad happens
     */
    public static JMXConnectorServer buildJmxConnector(final int startPort, final int maxRange, final MBeanServer mbs)
        throws IOException {
        final int nextOpenPort = PortScanner.getNextPort(startPort,30);
        log.log(Level.INFO, "[PORTSCAN]: using port " + nextOpenPort + " for next jmx interface");
        final JMXServiceURL url = new JMXServiceURL("jmxmp",null, nextOpenPort);
        final Map<String, Object> environment = new HashMap<>();
        final JMXConnectorServer connector = JMXConnectorServerFactory.newJMXConnectorServer(url,environment,mbs);
        return connector;
    }

    /**
     * Create a JMXMP Connector.
     * @param port port to run connector on
     * @param mbs desired mbean server to register to
     * @return JmxConnectorServer that could be started
     * @throws IOException io exception if something bad happens
     */
    public static JMXConnectorServer buildJmxConnector(final int port, final MBeanServer mbs)
            throws IOException {
        log.log(Level.INFO, "using port " + port + " for next jmx interface");
        final JMXServiceURL url = new JMXServiceURL("jmxmp",null, port);
        final Map<String, Object> environment = new HashMap<>();
        final JMXConnectorServer connector = JMXConnectorServerFactory.newJMXConnectorServer(url,environment,mbs);
        return connector;
    }

    /**
     * Create a JMXMP Connector and start it directly.
     * @param startPort startport to look for an open port
     * @param maxRange maximum port range check
     * @param mbs desired mbean server to register to
     * @return started JmxConnector
     * @throws IOException io exception if something bad happens
     */
    public static JMXConnectorServer buildAndStartJmxConnector(final int startPort, final int maxRange, final MBeanServer mbs)
            throws IOException {
        final JMXConnectorServer jmxConnectorServer = buildJmxConnector(startPort,maxRange,mbs);
        jmxConnectorServer.start();
        return jmxConnectorServer;
    }

    /**
     * Create a JMXMP Connector and start it directly.
     * @param port port to start service on
     * @param mbs desired mbean server to register to
     * @return started JmxConnector
     * @throws IOException io exception if something bad happens
     */
    public static JMXConnectorServer buildAndStartJmxConnector(final int port, final MBeanServer mbs)
            throws IOException {
        final JMXConnectorServer jmxConnectorServer = buildJmxConnector(port,mbs);
        jmxConnectorServer.start();
        return jmxConnectorServer;
    }

}
