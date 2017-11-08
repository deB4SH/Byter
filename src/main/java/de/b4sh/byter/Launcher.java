package de.b4sh.byter;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beust.jcommander.JCommander;
import de.b4sh.byter.client.Client;
import de.b4sh.byter.commander.Commander;
import de.b4sh.byter.configurationGenerator.Generator;
import de.b4sh.byter.example.future.CompletableFutureExample;
import de.b4sh.byter.server.Server;

/**
 * Launcher class contains every starting point for this application.
 * Starts either the server, client or nothing - based on the information given over the cli interface.
 */
public final class Launcher{
    private static final Logger log = Logger.getLogger(Launcher.class.getName());
    private static JCommander jcommander;
    //private static String[] globalArgs;
    private static Client client;
    private static Server server;
    private static Commander commander;
    private final CliParameter parameter;

    /**
     * check arguments on usage.
     * @param params
     */
    private Launcher(final CliParameter params){
        this.parameter = params;
        if("none".equals(this.parameter.service)){
            jcommander.usage();
        }
        this.run();
    }

    /**
     * Main Method.
     * @param argv passed arguments
     */
    public static void main(final String... argv) {

        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override public void run() {
                log.log(Level.INFO,"Received Shutdown Hook - Shuting down Server/Client");
                if(client != null)
                    client.stopJmxConnector();
                if(server != null)
                    server.stopJmxConnector();
                if(commander != null)
                    commander.stopJmxConnector();
            }
        });

        //globalArgs = argv;
        final CliParameter params = new CliParameter();
        jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        new Launcher(params);
    }

    /**
     * starts either the client or server.
     */
    private void run() {
        if("client".equals(this.parameter.service)){
            client = new Client(this.parameter,true);
        }
        if("server".equals(this.parameter.service)){
            server = new Server(this.parameter,true);
        }
        if("commander".equals(this.parameter.service)){
            commander = new Commander(this.parameter,true);
        }
        if("generator".equals(this.parameter.service)){
            final Generator generator = new Generator();
            generator.generateConfigurations();
        }
        if("cf".equals(this.parameter.service)){
            try {
                CompletableFutureExample.main(null);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
