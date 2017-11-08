/*
 * File: Generator
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-09
 * Type: Class
 */
package de.b4sh.byter.configurationGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.GsonBuilder;
import de.b4sh.byter.commander.config.ClientConfiguration;
import de.b4sh.byter.commander.config.ClientConnection;
import de.b4sh.byter.commander.config.ConfigurationHelper;
import de.b4sh.byter.commander.config.DirectConfiguration;
import de.b4sh.byter.commander.config.NetworkConfiguration;
import de.b4sh.byter.commander.config.ServerConfiguration;
import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.utils.data.TransformValues;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * Generator Class.
 * Helps to generate every possible combination for test-runs.
 * This class requires a connection.data file in the working directory to function properly.
 */
public final class Generator {

    private static final Logger log = Logger.getLogger(Generator.class.getName());
    private static final String outputDirectory = System.getProperty("user.dir") + File.separator + "configurations" + File.separator + "generator";

    /**
     * Constructor.
     */
    public Generator() {
        //NOP
    }

    /**
     * generate all possible configurations under given parameters.
     */
    public void generateConfigurations(){
        //check output folder
        if(!FileManager.isFolderExisting(outputDirectory))
            FileManager.createFolder(outputDirectory);
        //do the rest
        final String connectionDataString = new ConfigurationHelper().getConfigurationFromFile(new File(System.getProperty("user.dir"), "connection.data"));
        final ConnectionData cData = new GsonBuilder().create().fromJson(connectionDataString,ConnectionData.class);
        //constant values
        final String filePath = cData.getPath();
        final String username = "null";
        final String password = "null";
        final int measurementCount = 2500;
        //variable values
        int i = 0;
        //build network configuration
        for(DataAmount dataAmount: DataAmount.values()){
            for(GlobalBuffer globalBuffer: GlobalBuffer.values()){
                for(NetworkType nt: NetworkType.values()){ //server impl network
                    for(WriterType wt: WriterType.values()){ //server impl writer
                        //server configuration
                        final ServerConfiguration sc = new ServerConfiguration(cData.getServerIp(), cData.getServerPort(),
                                username,password,wt.getKey(),globalBuffer.getAmount(),nt.getKey(),globalBuffer.getAmount(),filePath);
                        //client configuration
                        final List<ClientConnection> clientConnections = new ArrayList<>();
                        clientConnections.add(new ClientConnection(cData.getClientIp(),cData.getClientPort(),username,password));
                        final ClientConfiguration cc = new ClientConfiguration(clientConnections,globalBuffer.getAmount(),
                                "unused_here",globalBuffer.getAmount(),dataAmount.getAmount());
                        //construct testname
                        final StringBuilder testNameBuilder = new StringBuilder(400);
                        testNameBuilder.append("network_").append(i).append("_").append(nt.getKey()).append("_")
                                .append(wt.getKey()).append("_buffer_").append(globalBuffer.amount)
                                .append("_trans_").append(dataAmount);
                        final NetworkConfiguration nc = new NetworkConfiguration(testNameBuilder.toString(),sc,cc,measurementCount);
                        i++;
                        final String exportString = new GsonBuilder().setPrettyPrinting().create().toJson(nc);
                        writeToFile(exportString, new File(outputDirectory,testNameBuilder.toString()+".network"));
                    }
                }
            }
        }
        //build direct configuration
        //static data
        final int writerCount = 1;

        //variable data
        i = 0;
        //run through
        for(DataAmount dataAmount: DataAmount.values()){
            for(GlobalBuffer globalBuffer: GlobalBuffer.values()){
                for(WriterType wt: WriterType.values()) { //client impl writer
                    //client configuration
                    final List<ClientConnection> clientConnections = new ArrayList<>();
                    clientConnections.add(new ClientConnection(cData.getClientIp(),cData.getClientPort(),username,password));
                    final ClientConfiguration cc = new ClientConfiguration(clientConnections,globalBuffer.getAmount(),
                            wt.getKey(),globalBuffer.getAmount(),dataAmount.getAmount());
                    //construct testname
                    final StringBuilder testNameBuilder = new StringBuilder(400);
                    testNameBuilder.append("direct_").append(i).append("_").append(wt.getKey())
                            .append("_buffer_").append(globalBuffer.amount).append("_trans_").append(dataAmount);
                    //direct configuration
                    final DirectConfiguration dc = new DirectConfiguration(testNameBuilder.toString(),cc,measurementCount,writerCount,filePath);
                    i++;
                    final String exportString = new GsonBuilder().setPrettyPrinting().create().toJson(dc);
                    writeToFile(exportString, new File(outputDirectory,testNameBuilder.toString()+".direct"));
                }
            }
        }
    }

    /**
     * Write given content to file.
     * @param content configuration content eg.
     * @param file file to write data to.
     */
    public static void writeToFile(final String content, final File file){
        try(
            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))
        ){
            outputStream.write(content.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during write of a String to a File. Check Stacktrace for issues.",e);
        }
    }

    /**
     * Scale all buffers at the same time to the same level.
     */
    enum GlobalBuffer{
        TenMB((int)(10*TransformValues.MEGABYTE)),
        OneMB((int)(1*TransformValues.MEGABYTE)),
        SixteyFourKB((int)(64*TransformValues.byteToKILOBYTE)),
        EightKB((int)(8*TransformValues.byteToKILOBYTE));

        private final int amount;

        GlobalBuffer(final int v) {
            this.amount = v;
        }

        public int getAmount() {
            return amount;
        }
    }


    /**
     * Server Writer Buffer Configuration Enum.
     */
    enum ServerWriterBuffer{
        TenMB((long)(10*TransformValues.MEGABYTE)),
        OneMB((long)(1*TransformValues.MEGABYTE)),
        SixteyFourKB((long)(64*TransformValues.byteToKILOBYTE)),
        EightKB((long)(8*TransformValues.byteToKILOBYTE));

        private final long amount;

        ServerWriterBuffer(final long v) {
            this.amount = v;
        }

        public long getAmount() {
            return amount;
        }
    }

    /**
     * Server Network Buffer Configuration Enum.
     */
    enum ServerNetworkBuffer{
        TenMB((long)(10*TransformValues.MEGABYTE)),
        OneMB((long)(1*TransformValues.MEGABYTE)),
        SixteyFourKB((long)(64*TransformValues.byteToKILOBYTE)),
        EightKB((long)(8*TransformValues.byteToKILOBYTE));

        private final long amount;

        ServerNetworkBuffer(final long v) {
            this.amount = v;
        }

        public long getAmount() {
            return amount;
        }
    }

    /**
     * Client Writer Buffer Configuration Enum.
     */
    enum ClientWriterBuffer{
        TenMB((long)(10*TransformValues.MEGABYTE)),
        OneMB((long)(1*TransformValues.MEGABYTE)),
        SixteyFourKB((long)(64*TransformValues.byteToKILOBYTE)),
        EightKB((long)(8*TransformValues.byteToKILOBYTE));

        private final long amount;

        ClientWriterBuffer(final long v) {
            this.amount = v;
        }

        public long getAmount() {
            return amount;
        }
    }

    /**
     * Client Network Buffer Configuration Enum.
     */
    enum ClientNetworkBuffer{
        TenMB((long)(10*TransformValues.MEGABYTE)),
        OneMB((long)(1*TransformValues.MEGABYTE)),
        SixteyFourKB((long)(64*TransformValues.byteToKILOBYTE)),
        EightKB((long)(8*TransformValues.byteToKILOBYTE));

        private final long amount;

        ClientNetworkBuffer(final long v) {
            this.amount = v;
        }

        public long getAmount() {
            return amount;
        }
    }

    /**
     * Dataamount Configuration Enum.
     */
    enum DataAmount{
        TenGB((long)(10 * TransformValues.GIGABYTE)),
        FiveGB((long)(5 * TransformValues.GIGABYTE)),
        TwoGB((long)(2 * TransformValues.GIGABYTE)),
        OneGB((long)(1 * TransformValues.GIGABYTE)),
        FiveHundretMB((long)(500 * TransformValues.MEGABYTE)),
        OneHundretMB((long)(100 * TransformValues.MEGABYTE));

        private final long amount;

        DataAmount(final long v) {
            this.amount = v;
        }

        public long getAmount() {
            return amount;
        }
    }


}
