package com.myecommerce.automation.driver.ports

interface HttpPort {
    fun <T : Any> getAs(path: String, type: Class<T>): T
    fun <T : Any> getListAs(path: String, elementType: Class<T>): List<T>
    fun <T : Any> getListWithQueryAs(path: String, paramName: String, paramValue: String, elementType: Class<T>): List<T>
}

inline fun <reified T : Any> HttpPort.getAs(path: String): T = getAs(path, T::class.java)
inline fun <reified T : Any> HttpPort.getListAs(path: String): List<T> = getListAs(path, T::class.java)
inline fun <reified T : Any> HttpPort.getListWithQueryAs(path: String, paramName: String, paramValue: String): List<T> =
    getListWithQueryAs(path, paramName, paramValue, T::class.java)
