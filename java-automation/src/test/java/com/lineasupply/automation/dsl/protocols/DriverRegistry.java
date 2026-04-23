package com.lineasupply.automation.dsl.protocols;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class DriverRegistry {

    private static final Map<String, Supplier<LineasupplyProtocol>> registry = new HashMap<>();

    public static void register(String channel, Supplier<LineasupplyProtocol> supplier) {
        registry.put(channel, supplier);
    }

    public static LineasupplyProtocol create(String channel) {
        var supplier = registry.get(channel);
        if (supplier == null) {
            loadByConvention(channel);
            supplier = registry.get(channel);
        }
        if (supplier == null) {
            throw new IllegalStateException(
                "No driver registered for channel '" + channel + "'. " +
                "Valid values: " + registry.keySet());
        }
        return supplier.get();
    }

    private static void loadByConvention(String channel) {
        var className = "com.lineasupply.automation.driver.%s.LineasupplyDriver"
            .formatted(channel.toLowerCase());
        try {
            Class.forName(className);
        } catch (ClassNotFoundException _) { }
    }

    private DriverRegistry() {}
}
