package cn.ggsn.openrxlight.transaction.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import cn.ggsn.openrxlight.request.PageRequest;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.TransactionStatus;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFilter extends PageRequest {
    private UUID sourceCounterpartyId;
    private UUID destinationCounterpartyId;
    private String counterpartyTxnId;
    private ChannelType sourceChannelType;
    private ChannelType destinationChannelType;
    private LocalDateTime completedTimeFrom;
    private LocalDateTime completedTimeTo;
    private List<TransactionType> transactionTypes;
    private List<TransactionStatus> transactionStatuses;
    private LocalDateTime createdTimeFrom;
    private LocalDateTime createdTimeTo;
    // if transactionIds is not empty, the page info will be ignored, set as default
    // pageNo=1, pageSize=size(transactionIds)
    private List<UUID> transactionIds;
}
