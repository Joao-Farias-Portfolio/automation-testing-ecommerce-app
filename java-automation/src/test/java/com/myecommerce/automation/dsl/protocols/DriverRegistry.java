package com.myecommerce.automation.dsl.protocols;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class DriverRegistry {

    private static final Map<Channel, Supplier<CatalogueProtocol>> registry = new HashMap<>();

    public static void register(Channel channel, Supplier<? extends CatalogueProtocol> supplier) {
        registry.put(channel, supplier::get);
    }

    public static CatalogueProtocol create(Channel channel) {
        var supplier = registry.get(channel);
        if (supplier == null) {
            loadByConvention(channel);
            supplier = registry.get(channel);
        }
        if (supplier == null) {
            throw new IllegalStateException(
                "No driver registered for channel '%s'. Valid values: %s"
                    .formatted(channel, registry.keySet()));
        }
        return supplier.get();
    }

    private static void loadByConvention(Channel channel) {
        var className = "com.myecommerce.automation.driver.%s.MyEcommerceDriver"
            .formatted(channel.name().toLowerCase());
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ignored) { }
    }

    private DriverRegistry() {}
}
