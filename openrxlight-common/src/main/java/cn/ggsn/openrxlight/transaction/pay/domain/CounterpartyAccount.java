package cn.ggsn.openrxlight.transaction.pay.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
@NoArgsConstructor
public abstract class CounterpartyAccount implements Validate {
    @Required
    @Setter
    private ChannelType channelType;
    @Required
    private UUID accountId;

    protected CounterpartyAccount(ChannelType channelType, UUID accountId) {
        this.channelType = channelType;
        this.accountId = accountId;
    }

    @JsonIgnore
    public abstract String getCounterpartyAccountId();
}
