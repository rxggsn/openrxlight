package cn.ggsn.openrxlight.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import cn.ggsn.openrxlight.lang.Lists2;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAccountList {
    private List<ExternalAccount> inner;

    public void add(ExternalAccount externalAccount) {
        if (this.inner == null) {
            this.inner = Lists2.empty();
        }
        this.inner.add(externalAccount);
    }
}
