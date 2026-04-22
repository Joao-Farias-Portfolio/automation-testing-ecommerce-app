package com.lineasupply.automation.driver;

import com.lineasupply.automation.driver.web.LineasupplyDriver;
import com.lineasupply.automation.dsl.LineasupplyProtocol;

public class DriverFactory {

    public static LineasupplyProtocol create() {
        String channel = System.getProperty("channel");
        if (channel == null || channel.isBlank()) {
            throw new IllegalStateException(
                "System property '-Dchannel' is required. Valid values: Web");
        }
        return switch (channel.trim()) {
            case "Web" -> new LineasupplyDriver();
            default    -> throw new IllegalStateException(
                "Unknown channel '" + channel + "'. Valid values: Web");
        };
    }
}
