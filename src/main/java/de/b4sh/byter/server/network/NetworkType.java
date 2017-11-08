package de.b4sh.byter.server.network;

/**
 * ENum for supported service implementations.
 */
public enum NetworkType {
    None("none"),
    BufferedNetwork("buff"),
    DataInput("data"),
    BufferedWorkpile("buwp");

    private final String key;

    /**
     * Constructor for Key on Value.
     * @param value key.
     */
    NetworkType(final String value) {
        this.key = value;
    }

    /**
     * Get all possible options available.
     * @return String with all options possible
     */
    public static String getOptionList() {
        final StringBuilder sb = new StringBuilder();
        sb.append("You passed a wrong Network Type. Please chose one of those. \n");
        for(NetworkType n: NetworkType.values()){
            sb.append(n.name()); sb.append(" | key: "); sb.append(n.getKey()); sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Get key for value.
     * @return String with key
     */
    public String getKey(){
        return this.key;
    }

    /**
     * Checks if the implementation given is supported or available by this type enum.
     * @param key key to check for
     * @return true | false (not found)
     */
    public static boolean isImplementationAvailable(final String key){
        for(NetworkType nt: NetworkType.values()){
            if(key.equals(nt.getKey()))
                return true;
        }
        return false;
    }
}
