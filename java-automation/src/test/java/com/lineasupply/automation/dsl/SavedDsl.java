package com.lineasupply.automation.dsl;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class SavedDsl {

    private static final Logger log = Logger.getLogger(SavedDsl.class.getName());

    private final LineasupplyProtocol protocol;
    private boolean initialSaveState;

    public SavedDsl(LineasupplyProtocol protocol) {
        this.protocol = protocol;
    }

    public void navigateHomeAndWaitForSaveButtons() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("navigated to homepage and waited for save buttons");
    }

    public void recordInitialSaveState() {
        initialSaveState = protocol.getSavedState().saveButtonPressed();
        log.fine("recorded initial save state: pressed=" + initialSaveState);
    }

    public void ensureFirstProductIsSaved() {
        if (!protocol.getSavedState().saveButtonPressed()) {
            protocol.toggleFirstSaveButton();
            log.fine("toggled save button to saved state");
        } else {
            log.fine("first product already saved; no toggle needed");
        }
    }

    public void openSavedPage() {
        protocol.openSavedPage();
        protocol.waitForSavedPageToLoad();
        log.fine("navigated to saved page");
    }

    public void toggleFirstSaveButton() {
        protocol.toggleFirstSaveButton();
        log.fine("toggled first save button");
    }

    public void clickWishlistLink() {
        protocol.clickWishlistLink();
        log.fine("clicked wishlist link");
    }

    public void assertSaveStateChanged() {
        boolean currentState = protocol.getSavedState().saveButtonPressed();
        assertThat(currentState)
            .as("save state of first product should have changed from initial state (" + initialSaveState + ")")
            .isNotEqualTo(initialSaveState);
        log.fine("save state changed from " + initialSaveState + " to " + currentState);
    }

    public void assertSaveStateRestored() {
        boolean currentState = protocol.getSavedState().saveButtonPressed();
        assertThat(currentState)
            .as("save state should be restored to initial state (" + initialSaveState + ")")
            .isEqualTo(initialSaveState);
        log.fine("save state restored to initial: " + currentState);
    }

    public void assertSavedCountIsVisible() {
        int count = protocol.getSavedState().savedPageCount();
        assertThat(count)
            .as("saved count should be visible and show a number >= 1")
            .isGreaterThanOrEqualTo(1);
        log.fine("saved count is visible: " + count);
    }

    public void assertWishlistLinkVisible() {
        assertThat(protocol.getSavedState().wishlistLinkVisible())
            .as("wishlist link should be visible on the saved page")
            .isTrue();
        log.fine("wishlist link is visible");
    }

    public void assertUrlContainsSaved() {
        assertThat(protocol.currentUrl())
            .as("URL should contain '/saved'")
            .contains("/saved");
        log.fine("URL contains /saved");
    }

    public void assertSaveButtonVisibleAndFunctional() {
        var state = protocol.getSavedState();
        assertThat(state.saveButtonPresent())
            .as("save button should be present and displayed")
            .isTrue();
        assertThat(state.saveButtonEnabled())
            .as("save button should be enabled")
            .isTrue();
        log.fine("save button is present and enabled");
    }
}
