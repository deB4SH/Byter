/*
 * File: CommanderMode
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-31
 * Type: Enum
 */
package de.b4sh.byter.commander;

/**
 * Describes in which mode the commander should operate.
 * eg. for network based tests it need a connection to both services.
 *  for a direct test is just needs a client connection and is good to go
 */
public enum CommanderMode {
    Direct("direct"),
    Network("network");

    private String mode;

    /**
     * package-private constructor for CommanderMode.
     * @param mode mode the commander can operate with
     */
    CommanderMode(final String mode) {
        this.mode = mode;
    }

    /**
     * get the actual key to reference this mode.
     * @return String with the mode
     */
    public String getKey(){
        return this.mode;
    }

    /**
     * get the corresponding mode to a given key.
     * @param key key for the required mode
     * @return CommanderMode or null (when type not known)
     */
    public CommanderMode getModeByKey(final String key){
        for (CommanderMode cm: CommanderMode.values()){
            if(cm.getKey().equals(key)){
                return cm;
            }
        }
        return null;
    }
}
