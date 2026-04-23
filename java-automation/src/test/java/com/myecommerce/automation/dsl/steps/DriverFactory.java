package com.lineasupply.automation.dsl.steps;

import com.lineasupply.automation.dsl.protocols.DriverRegistry;
import com.lineasupply.automation.dsl.protocols.LineasupplyProtocol;

public class DriverFactory {

    public static LineasupplyProtocol create() {
        String channel = System.getProperty("channel");
        if (channel == null || channel.isBlank()) {
            throw new IllegalStateException(
                "System property '-Dchannel' is required. Valid values: Web");
        }
        return DriverRegistry.create(channel.trim());
    }
}
