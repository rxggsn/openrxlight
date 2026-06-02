package cn.ggsn.rxlight.account.event;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchCreateAccountEvent {
    private List<Account> accounts;
}
