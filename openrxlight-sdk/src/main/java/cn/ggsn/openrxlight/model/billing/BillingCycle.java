package cn.ggsn.openrxlight.model.billing;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public enum BillingCycle {
    WEEKLY((short) 1),
    MONTHLY((short) 2),
    YEARLY((short) 3),
    NONE((short) 0);

    private final short value;

    public static BillingCycle fromValue(int value) {
        for (BillingCycle cycle : BillingCycle.values()) {
            if (cycle.value == value) {
                return cycle;
            }
        }
        return NONE;
    }

    public String getDescription(String language) {
        switch (this) {
            case WEEKLY:
                return StringUtils.startsWith(language, "en") ? "Weekly" : "每周";
            case MONTHLY:
                return StringUtils.startsWith(language, "en") ? "Monthly" : "每月";
            case YEARLY:
                return StringUtils.startsWith(language, "en") ? "Yearly" : "每年";
            default:
                return StringUtils.startsWith(language, "en") ? "None" : "无";
        }
    }
}
