package de.b4sh.byter.commander.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.b4sh.byter.utils.io.FileManager;

/**
 * Helper class to read configuration files from space!
 * SPAAAAAAAAAAAAACE!
 */
public final class ConfigurationHelper {
    private static final Logger log = Logger.getLogger(ConfigurationHelper.class.getName());
    private final Gson gson;
    /**
     * private constructor for ConfigurationHelper.
     */
    public ConfigurationHelper(){
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Read all configurations inside a folder.
     * @param filePath filepath to folder to read
     * @return List of configurations found
     */
    List<NetworkConfiguration> readNetworkConfigurations(final File filePath){
        final List<NetworkConfiguration> configurations = new ArrayList<>();
        final List<File> activeConfigurations = FileManager.getFilesFilteredByExtension(filePath,"network");
        this.translateFromFileToNetworkConfiguration(configurations,activeConfigurations);
        return configurations;
    }

    /**
     * Read all configurations inside a folder.
     * @param filePath filepath to folder to read
     * @return List of configurations found
     */
    List<DirectConfiguration> readDirectConfigurations(final File filePath){
        final List<DirectConfiguration> configurations = new ArrayList<>();
        final List<File> activeConfigurations = FileManager.getFilesFilteredByExtension(filePath,"direct");
        this.translateFromFileToDirectConfiguration(configurations,activeConfigurations);
        return configurations;
    }

    /**
     * Read all network configurations inside a folder.
     * @param filePath filepath to folder to read
     * @return List of configurations found
     */
    public List<NetworkConfiguration> readNetworkConfigurations(final String filePath){
        return readNetworkConfigurations(new File(filePath));
    }

    /**
     * Read all network configurations inside a folder.
     * @param filePath filepath to folder to read
     * @return List of configurations found
     */
    public List<DirectConfiguration> readDirectConfigurations(final String filePath){
        return readDirectConfigurations(new File(filePath));
    }

    /**
     * Read configurations that contains the given name.
     * @param filePath file path to scan for files.
     * @param containsName String that the filename should
     * @return List with configurations found under the given file name they should contain
     */
    public List<NetworkConfiguration> readConfigurationsByName(final File filePath, final String containsName){
        final List<NetworkConfiguration> configurations = new ArrayList<>();
        final List<File> activeConfigurations = FileManager.getFilesFiltered(filePath,containsName);
        this.translateFromFileToNetworkConfiguration(configurations,activeConfigurations);
        return configurations;
    }

    /**
     * Read configurations that contains the given name.
     * @param filePath file path to scan for files.
     * @param containsName String that the filename should
     * @return List with configurations found under the given file name they should contain
     */
    public List<NetworkConfiguration> readConfigurationsByName(final String filePath, final String containsName){
        return readConfigurationsByName(new File(filePath), containsName);
    }

    /**
     * Translates Network Configurations from their related files into RootConfigurations for this application.
     * @see NetworkConfiguration
     * @param configurations configuration list that should be written to
     * @param files files you want to convert to NetworkConfiguration
     */
    private void translateFromFileToNetworkConfiguration(final List<NetworkConfiguration> configurations, final List<File> files){
        for(File f: files){
            final String currentConfiguration = getConfigurationFromFile(f);
            //nullcheck
            if(currentConfiguration == null)
                log.log(Level.WARNING, "Current Configuration is null. Issue in getConfigurationFromFile with File: " + f.getName());
            configurations.add(this.gson.fromJson(currentConfiguration,NetworkConfiguration.class));
        }
    }

    /**
     * Translates Network Configurations from their related files into RootConfigurations for this application.
     * @see DirectConfiguration
     * @param configurations configuration list that should be written to
     * @param files files you want to convert to NetworkConfiguration
     */
    private void translateFromFileToDirectConfiguration(final List<DirectConfiguration> configurations, final List<File> files){
        for(File f: files){
            final String currentConfiguration = getConfigurationFromFile(f);
            //nullcheck
            if(currentConfiguration == null)
                log.log(Level.WARNING, "Current Configuration is null. Issue in getConfigurationFromFile with File: " + f.getName());
            configurations.add(this.gson.fromJson(currentConfiguration,DirectConfiguration.class));
        }
    }

    /**
     * Read content from file with BufferedReader.
     * @param pathToFile path to the file to read.
     * @return String with configuration information.
     */
    public String getConfigurationFromFile(final File pathToFile){
        try {
            final BufferedReader bfr = new BufferedReader(new FileReader(pathToFile));
            final StringBuilder sb = new StringBuilder();
            String curLine = bfr.readLine();
            while(curLine != null){
                sb.append(curLine);
                sb.append(System.lineSeparator());
                curLine = bfr.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING, "Cant find file.. even after i queried 2 ns for it! WHAT?",e);
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during reading configuration file in.");
        }
        return null;
    }
}
