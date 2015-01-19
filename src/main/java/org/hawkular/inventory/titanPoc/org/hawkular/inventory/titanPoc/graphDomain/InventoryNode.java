package org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain;

import java.util.Set;

/**
 * Created by jkremser on 1/19/15.
 */
public interface InventoryNode {
    Object id();
    String getProperty(String key);
    void setProperty(String key, String property);
    Set<String> getPropertyKeys();
}
