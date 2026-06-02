package cn.ggsn.rxlight.ai.error;

import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;

@Getter
public enum AiError implements ErrorCode {
    AgentPortfolioNotFound(1, "Agent portfolio not found"),;

    private final int value;
    private final String message;

    AiError(int value, String message) {
        this.value = value;
        this.message = message;
    }

}
