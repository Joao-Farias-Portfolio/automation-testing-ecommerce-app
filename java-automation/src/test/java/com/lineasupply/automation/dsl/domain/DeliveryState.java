package com.lineasupply.automation.dsl.domain;

import java.util.List;

public record DeliveryState(
    boolean sectionVisible,
    List<DeliveryOption> options,
    String headerText,
    boolean minimumOrderTextPresent
) {
    public long selectedOptionCount() {
        return options.stream().filter(DeliveryOption::selected).count();
    }
}
