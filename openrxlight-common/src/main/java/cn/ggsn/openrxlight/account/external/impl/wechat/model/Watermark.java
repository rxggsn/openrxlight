package cn.ggsn.openrxlight.account.external.impl.wechat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watermark {
    private String timestamp;
    private String appid;
}
