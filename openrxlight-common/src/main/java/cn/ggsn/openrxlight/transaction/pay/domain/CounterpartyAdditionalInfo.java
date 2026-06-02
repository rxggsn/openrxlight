package cn.ggsn.openrxlight.transaction.pay.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
public interface CounterpartyAdditionalInfo {
}
