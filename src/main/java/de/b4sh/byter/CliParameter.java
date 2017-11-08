package de.b4sh.byter;

import java.io.File;

import com.beust.jcommander.Parameter;

/**
 * Console Parameter Class.
 * defines parameters that should be parsed.
 */
@SuppressWarnings("checkstyle:visibilitymodifiercheck")
public final class CliParameter {

    /**
     * set which service to use.
     */
    @Parameter(names = { "-s", "--service"}, description = "decide which service should be started [client,server,commander]")
    public String service = "none";

    /**
     * set if there should be a increased console verbosity level.
     */
    @Parameter(names = { "-v", "--verbose" }, description = "set the level of verbosity")
    public Integer verbose = 1;

    /**
     * set if jmx should be enabled.
     * if disabled the whole sense of this application is set to client sided direct io tests.
     */
    @Parameter(names = { "-j", "--jmx"}, description = "should jmx start? default: true")
    public boolean jmx = true;

    /**
     * set the standard jmx port to communicate on.
     */
    @Parameter(names = { "-jp", "--jmxport"}, description = "port on jmx should listen. default: 61000")
    public Integer jmxPort = 61000;

    /**
     * sets the path to configurations.
     */
    @Parameter(names = { "-cfg", "--config"}, description = "configuration path for commander")
    public String configPath = System.getProperty("user.dir") + File.separator + "configurations" + File.separator + "live";

    /**
     * Empty constructor, fields are public and usable for launch parameters.
     */
    public CliParameter(){
        //nop
    }
}