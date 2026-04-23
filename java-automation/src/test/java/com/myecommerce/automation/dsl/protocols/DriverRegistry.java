package com.myecommerce.automation.dsl.protocols;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class DriverRegistry {

    private static final Map<String, Supplier<MyEcommerceProtocol>> registry = new HashMap<>();

    public static void register(String channel, Supplier<MyEcommerceProtocol> supplier) {
        registry.put(channel, supplier);
    }

    public static MyEcommerceProtocol create(String channel) {
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
        var className = "com.myecommerce.automation.driver.%s.MyEcommerceDriver"
            .formatted(channel.toLowerCase());
        try {
            Class.forName(className);
        } catch (ClassNotFoundException _) { }
    }

    private DriverRegistry() {}
}
