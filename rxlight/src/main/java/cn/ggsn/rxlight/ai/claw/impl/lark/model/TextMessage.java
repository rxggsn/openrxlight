package cn.ggsn.rxlight.ai.claw.impl.lark.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextMessage {
    private String text;
}
