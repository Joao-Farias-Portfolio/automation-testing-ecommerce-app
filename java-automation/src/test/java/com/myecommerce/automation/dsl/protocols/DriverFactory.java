package com.myecommerce.automation.dsl.protocols;

import com.myecommerce.automation.dsl.protocols.DriverRegistry;
import com.myecommerce.automation.dsl.protocols.MyEcommerceProtocol;

public class DriverFactory {

    public static MyEcommerceProtocol create() {
        String channel = System.getProperty("channel");
        if (channel == null || channel.isBlank()) {
            throw new IllegalStateException(
                "System property '-Dchannel' is required. Valid values: Web");
        }
        return DriverRegistry.create(channel.trim());
    }
}
