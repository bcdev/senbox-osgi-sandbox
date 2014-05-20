package org.teo.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Norman on 20.02.14.
 */
public class OperatorRegistry {
    public static OperatorRegistry INSTANCE = new OperatorRegistry();
    private Map<String, OperatorSpi > operatorSpis = new HashMap<>();

    public Map<String, OperatorSpi> getOperatorSpis() {
        return Collections.unmodifiableMap(operatorSpis);
    }

    public void add(OperatorSpi operatorSpi) {
        operatorSpis.put(operatorSpi.getName(), operatorSpi);
    }

    public void remove(OperatorSpi operatorSpi) {
        remove(operatorSpi.getName());
    }
    public void remove(String id) {
        operatorSpis.remove(id);
    }

    private OperatorRegistry() {
    }
}
