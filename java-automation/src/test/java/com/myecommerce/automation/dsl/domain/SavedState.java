package com.myecommerce.automation.dsl.domain;

public record SavedState(
    boolean saveButtonPresent,
    boolean saveButtonPressed,
    boolean saveButtonEnabled,
    int savedPageCount,
    boolean wishlistLinkVisible
) {}
