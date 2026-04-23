package com.myecommerce.automation.dsl.protocols;

public class DriverFactory {

    public static CatalogueProtocol createCatalogue() {
        return DriverRegistry.create(Channel.current());
    }

    public static CartProtocol createCart() {
        var driver = DriverRegistry.create(Channel.current());
        if (!(driver instanceof CartProtocol cartProtocol)) {
            throw new IllegalStateException(
                "Channel '%s' does not support cart operations".formatted(Channel.current()));
        }
        return cartProtocol;
    }

    public static SavedProtocol createSaved() {
        var driver = DriverRegistry.create(Channel.current());
        if (!(driver instanceof SavedProtocol savedProtocol)) {
            throw new IllegalStateException(
                "Channel '%s' does not support saved/wishlist operations".formatted(Channel.current()));
        }
        return savedProtocol;
    }
}
