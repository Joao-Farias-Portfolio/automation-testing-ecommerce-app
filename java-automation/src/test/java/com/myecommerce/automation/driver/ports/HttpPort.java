package com.myecommerce.automation.driver.ports;

import java.util.List;

public interface HttpPort {

    <T> T getAs(String path, Class<T> type);
    <T> List<T> getListAs(String path, Class<T> elementType);
    <T> List<T> getListWithQueryAs(String path, String paramName, String paramValue, Class<T> elementType);
}
