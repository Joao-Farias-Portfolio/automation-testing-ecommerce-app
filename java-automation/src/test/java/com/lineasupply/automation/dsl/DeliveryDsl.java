package com.lineasupply.automation.dsl;

import org.assertj.core.api.SoftAssertions;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryDsl {

    private static final Logger log = Logger.getLogger(DeliveryDsl.class.getName());

    private final LineasupplyProtocol protocol;

    public DeliveryDsl(LineasupplyProtocol protocol) {
        this.protocol = protocol;
    }

    public void navigateToFirstProductDetailPage() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        protocol.clickFirstProductCard();
        log.fine("navigated to first product detail page");
    }

    public void selectAlternativeOption() {
        protocol.selectAlternativeDeliveryOption();
        log.fine("selected alternative delivery option");
    }

    public void assertDeliverySectionVisible() {
        assertThat(protocol.getDeliveryState().sectionVisible())
            .as("delivery section should be visible on the product detail page")
            .isTrue();
        log.fine("delivery section is visible");
    }

    public void assertDeliverySectionHasRadioOptions() {
        var options = protocol.getDeliveryState().options();
        assertThat(options)
            .as("delivery section should contain radio button options")
            .isNotEmpty();
        log.fine("delivery section has " + options.size() + " radio options");
    }

    public void assertOneOptionSelectedByDefault() {
        var state = protocol.getDeliveryState();
        assertThat(state.sectionVisible())
            .as("delivery section must be visible")
            .isTrue();
        long selectedCount = state.options().stream().filter(o -> o.selected()).count();
        assertThat(selectedCount)
            .as("exactly one delivery option should be selected by default")
            .isEqualTo(1L);
        log.fine("exactly one delivery option is selected by default");
    }

    public void assertAlternativeOptionIsSelected() {
        var state = protocol.getDeliveryState();
        assertThat(state.sectionVisible())
            .as("delivery section must be visible")
            .isTrue();
        long selectedCount = state.options().stream().filter(o -> o.selected()).count();
        assertThat(selectedCount)
            .as("exactly one delivery option should be selected after changing")
            .isEqualTo(1L);
        log.fine("a delivery option is selected after changing");
    }

    public void assertNoMinimumOrderRestrictions() {
        assertThat(protocol.getDeliveryState().minimumOrderTextPresent())
            .as("no minimum order restrictions should be shown")
            .isFalse();
        log.fine("no minimum order restrictions are shown");
    }

    public void assertDeliverySectionHasHeader() {
        var state = protocol.getDeliveryState();
        if (!state.sectionVisible()) {
            log.fine("delivery section not visible; skipping header check");
            return;
        }
        assertThat(state.headerText())
            .as("delivery section header should contain 'delivery options'")
            .isNotBlank();
        log.fine("delivery section header: '" + state.headerText() + "'");
    }

    public void assertProductDetailPageFunctional() {
        var detail = protocol.getProductDetail();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(detail.title()).as("product title should be present").isNotBlank();
        soft.assertThat(detail.price()).as("product price should be present").isNotBlank();
        soft.assertThat(detail.addToCartButtonText()).as("add-to-cart button should be present").isNotBlank();
        soft.assertAll();
        log.fine("product detail page is functional: title, price, and add-to-cart button present");
    }
}
