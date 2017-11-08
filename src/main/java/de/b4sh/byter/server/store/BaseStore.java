/*
 * File: BaseStore
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-03
 * Type: Class
 */
package de.b4sh.byter.server.store;

import javax.management.ObjectName;

import de.b4sh.byter.utils.jmx.JmxUtils;

/**
 * Abstract base class for all storage implementations.
 * This class defines everything that is required to create a user-build storetype.
 */
public abstract class BaseStore extends Thread {

    private final StoreType type;
    private final ObjectName objectName;

    /**
     * protected constructor for BaseStore.
     * @param type passed type
     */
    protected BaseStore(final StoreType type) {
        this.type = type;
        this.objectName = JmxUtils.objectName(type.toString(),true,"storage");
    }

    /**
     * boot up serveral things - that are maybe required to have this store running.
     */
    abstract void boot();

    /**
     * shutdown all functions of this store.
     */
    abstract void shutdown();

    /**
     * Return the storeType.
     * @return StoreType
     */
    public final StoreType getStoreType(){
        return this.type;
    }

    /**
     * get the set object name.
     * @return ObjectName
     */
    public final ObjectName getObjectName(){
        return this.objectName;
    }

    /**
     * Passthrough method to get the DONE-state of the currently used implementation.
     * @return boolean
     */
    public abstract boolean isImplementationDone();

}
