package cn.ggsn.openrxlight.transaction.wallet.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.error.TpsErrorCode;
import cn.ggsn.openrxlight.transaction.pay.domain.CounterpartyAccount;

@Getter
@NoArgsConstructor
public class WalletCounterpartyAccount extends CounterpartyAccount {
    private UUID rxdomainAccountId;

    WalletCounterpartyAccount(ChannelType channelType, UUID rxdomainAccountId) {
        super(channelType, rxdomainAccountId);
        this.rxdomainAccountId = rxdomainAccountId;
    }

    @Override
    public String getCounterpartyAccountId() {
        return this.rxdomainAccountId.toString();
    }

    public static WalletCounterpartyAccount depositAccount(ChannelType channelType, UUID rxdomainAccountId) {
        if (ChannelType.WALLET.equals(channelType) || ChannelType.UNSPECIFIED.equals(channelType)) {
            throw new BizException(TpsErrorCode.NotSupportDepositChannel, channelType.name());
        }
        return new WalletCounterpartyAccount(channelType, rxdomainAccountId);
    }

    public static WalletCounterpartyAccount walletAccount(UUID rxdomainAccountId) {
        return new WalletCounterpartyAccount(ChannelType.WALLET, rxdomainAccountId);
    }
}
