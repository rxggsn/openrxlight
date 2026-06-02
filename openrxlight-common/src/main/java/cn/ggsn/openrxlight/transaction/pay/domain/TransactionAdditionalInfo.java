package cn.ggsn.openrxlight.transaction.pay.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import cn.ggsn.openrxlight.request.Validate;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public interface TransactionAdditionalInfo extends Validate {

}
