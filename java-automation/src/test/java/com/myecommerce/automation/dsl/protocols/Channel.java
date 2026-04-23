package com.myecommerce.automation.dsl.protocols;

import org.apache.commons.lang3.StringUtils;

public enum Channel {
    WEB("Web"), API("API");

    public final String registrationKey;

    Channel(String registrationKey) {
        this.registrationKey = registrationKey;
    }

    private static Channel instance;

    public static Channel current() {
        if (instance == null) {
            instance = resolve();
        }
        return instance;
    }

    private static Channel resolve() {
        String raw = readChannelProperty();
        return parse(raw);
    }

    private static String readChannelProperty() {
        String raw = System.getProperty("channel");
        if (StringUtils.isBlank(raw)) {
            throw new IllegalStateException(
                "System property '-Dchannel' is required. Valid values: Web, API");
        }
        return raw.trim();
    }

    private static Channel parse(String raw) {
        try {
            return Channel.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "Unknown channel '" + raw + "'. Valid values: Web, API");
        }
    }
}
