package com.myecommerce.automation.dsl.protocols;

public class DriverFactory {

    public static MyEcommerceProtocol create() {
        return DriverRegistry.create(Channel.current().registrationKey);
    }
}
