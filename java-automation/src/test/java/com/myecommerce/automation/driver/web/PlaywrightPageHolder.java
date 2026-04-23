package com.myecommerce.automation.driver.web;

import com.microsoft.playwright.Page;

public final class PlaywrightPageHolder {

    private static final ThreadLocal<Page> HOLDER = new ThreadLocal<>();

    private PlaywrightPageHolder() {}

    public static void set(Page page) {
        HOLDER.set(page);
    }

    public static Page get() {
        Page page = HOLDER.get();
        if (page == null) throw new IllegalStateException("No Playwright Page set for this thread");
        return page;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
