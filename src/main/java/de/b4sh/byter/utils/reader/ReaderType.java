/*
 * File: ReaderType
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-09-27
 * Type: Class
 */
package de.b4sh.byter.utils.reader;

/**
 * Enum for all possible reader types.
 */
public enum  ReaderType {
    none("none"),
    rafr("rafr");

    private final String type;

    /**
     * Constructor.
     * @param type type of the reader
     */
    ReaderType(final String type) {
        this.type = type;
    }

    /**
     * get the writer key.
     * @return String with key.
     */
    public String getType() {
        return type;
    }

    /**
     * check if the reqeusted type is registered on this type list.
     * @param type type to check
     * @return true | false
     */
    public static boolean isTypeRegistered(final String type){
        for(final ReaderType rt: ReaderType.values()){
            if(type.equals(rt.getType()))
                return true;
        }
        return false;
    }

    /**
     * gets the ReaderType for set key.
     * @param key key for required reader type
     * @return reader type | null (if not found)
     */
    public static ReaderType getTypeByKey(final String key){
        for(final ReaderType rt: ReaderType.values()){
            if(key.equals(rt.getType()))
                return rt;
        }
        return null;
    }
}
