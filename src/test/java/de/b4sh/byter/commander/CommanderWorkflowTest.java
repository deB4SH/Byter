package de.b4sh.byter.commander;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.beust.jcommander.JCommander;
import de.b4sh.byter.CliParameter;
import de.b4sh.byter.client.Client;
import de.b4sh.byter.server.Server;
import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.utils.io.FileManager;

public final class CommanderWorkflowTest {
    private static final Logger log = Logger.getLogger(CommanderWorkflowTest.class.getName());
    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "commander_workflow_test";
    private static String testSpaceDirectoryDirect = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "commander_direct_test";
    private static String testSpaceDirectoryNetwork = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "commander_network_test";
    private static ExecutorService service;
    private static Server serverObj;
    private static Client clientObj;
    private static Commander commanderObj;


    @BeforeClass
    public static void startTestEnvironment(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDirectory);
        FileManager.createFolder(testSpaceDirectoryDirect);
        FileManager.createFolder(testSpaceDirectoryNetwork);
        service = Executors.newFixedThreadPool(2);
        serverObj = ComponentHelper.startServer(service);
        clientObj = ComponentHelper.startClient(service);
    }

    @AfterClass
    public static void shutdownTestEnvironment(){
        //shutdown jmx and service
        ComponentHelper.shutdownService(service);
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDirectory);
        if(FileManager.countFilesInDirectory(testSpaceDirectoryDirect) > 0){
            //FileManager.removeAllFilesInDirectory(testSpaceDirectoryDirect);
        }else{
            log.log(Level.INFO, "there wasnt any file in the directory, either the writer deleted it already (good) or was there any exception? (bad)");
        }
        if(FileManager.countFilesInDirectory(testSpaceDirectoryNetwork) > 0){
            //FileManager.removeAllFilesInDirectory(testSpaceDirectoryNetwork);
        }else{
            log.log(Level.INFO, "there wasnt any file in the directory, either the writer deleted it already (good) or was there any exception? (bad)");
        }

    }

    @Test
    public void directCommanderWorkflow(){
        final String configurationDirectory = System.getProperty("user.dir")
                                            + File.separator + "configurations" + File.separator + "test"
                                            + File.separator + "direct";
        final String[] argv = new String[]{"-s","commander","-cfg",configurationDirectory};
        this.startService(argv);
    }

    @Test
    public void networkCommanderWorkflow(){
        final String configurationDirectory = System.getProperty("user.dir")
                + File.separator + "configurations" + File.separator + "test"
                + File.separator + "network";
        final String[] argv = new String[]{"-s","commander","-cfg",configurationDirectory};
        this.startService(argv);
    }

    private void startService(final String[] argv){
        final CliParameter params = new CliParameter();
        JCommander jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        //build client runnable
        final Commander commander = new Commander(params,false);
        commander.setTest(serverObj.getConnectorSystemPort(),clientObj.getConnectorSystemPort());
        final Thread commanderThread = new Thread(commander);
        commanderThread.run();
    }
}
