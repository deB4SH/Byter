/*
 * File: DirectConfig
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-31
 * Type: Class
 */
package de.b4sh.byter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import de.b4sh.byter.commander.config.ClientConfiguration;
import de.b4sh.byter.commander.config.ClientConnection;
import de.b4sh.byter.commander.config.NetworkConfiguration;
import de.b4sh.byter.commander.config.ServerConfiguration;
import de.b4sh.byter.support.JUnitFileWriter;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.io.FileManager;

public class NetworkConfigurationTest {

    private static Logger log = Logger.getLogger(NetworkConfigurationTest.class.getName());
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "configuration_network_test";

    /**
     * Start Method for everything related to this Test-Case.
     */
    @BeforeClass
    public static void startup() {
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDir);
    }

    @AfterClass
    public static void endTest() {
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    @Test
    public void testNetworkConfigurationCreation() {
        final ServerConfiguration sc = new ServerConfiguration("host.local", 12345,
                "username", "password","buff",1234,
                "buff",1234,testSpaceDir);
        final List<ClientConnection> clients = new ArrayList<>();
        clients.add(new ClientConnection("localhost",61000,"null","null"));
        clients.add(new ClientConnection("localhost",61001,"null","null"));
        final ClientConfiguration cc = new ClientConfiguration(clients, 1234, "buff", 8192, 64000);
        for (int i = 0; i < 10; i++) {
            final File outFile = new File(testSpaceDir, StringGenerator.nextRandomString(3) + "_" + i + ".network");
            final NetworkConfiguration nc = new NetworkConfiguration("abcd" + i, sc,cc,1);
            final String testExport = new GsonBuilder().setPrettyPrinting().create().toJson(nc);
            JUnitFileWriter.writeToFile(testExport, outFile);
        }
        List<File> files = FileManager.getFiles(testSpaceDir);
        Assert.assertEquals(10, files.size());
    }

    @Test
    public void testReadBackOfConfigurations(){
        //TODO
    }
}
