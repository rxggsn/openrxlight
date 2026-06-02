package cn.ggsn.openrxlight.account.domain;

import lombok.Getter;

import java.util.Map;

import cn.ggsn.openrxlight.lang.Maps2;

@Getter
public class AdditionalInfo {

    private Map<String, Object> externalConfiguration;

    public Object getExternalConfigurationByKey(String key) {
        if (Maps2.isEmpty(this.externalConfiguration)) {
            return null;
        }
        return this.externalConfiguration.get(key);
    }

    public void setExternalConfiguration(Map<String, Object> keyValues) {
        keyValues.forEach((key, val) -> {
            if (this.externalConfiguration == null) {
                this.externalConfiguration = Maps2.empty();
            }
            this.externalConfiguration.put(key, val);
        });
    }
}
