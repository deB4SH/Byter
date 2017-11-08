/*
 * File: WriterTye
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-03
 * Type: Enum
 */
package de.b4sh.byter.utils.writer;

/**
 * ENum for Writer.
 * all supported types on the server side.
 */
public enum WriterType {
    None("none"),
    BufferedWriter("buff"),
    FileChannelWriter("fich"),
    NullWriter("null"),
    RAFWriter("rafw"),
    Archival("arch");

    private final String key;

    /**
     * Constructor for Enum Key.
     * @param value key to present.
     */
    WriterType(final String value) {
        this.key = value;
    }

    /**
     * Get a String List of all possible Options.
     * @return String with a list content
     */
    public static String getOptionList(){
        final StringBuilder sb = new StringBuilder();
        sb.append("You passed a wrong Writer Type. Please chose one of those. \n");
        for(WriterType n: WriterType.values()){
            sb.append(n.name()); sb.append(" | key: "); sb.append(n.getKey()); sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * get key of type.
     * @return String
     */
    public String getKey(){
        return this.key;
    }

    /**
     * Checks if the requested implementation is available.
     * @param impl requested implementation
     * @return true | false (not found)
     */
    public static boolean isImplementationAvailable(String impl) {
        for(WriterType wt: WriterType.values()){
            if(impl.equals(wt.getKey()))
                return true;
        }
        return false;
    }
}
