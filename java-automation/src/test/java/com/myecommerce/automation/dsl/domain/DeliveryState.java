package com.myecommerce.automation.dsl.domain;

import lombok.Builder;

import java.util.List;

@Builder
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
