package cn.ggsn.openrxlight.errorx;

import lombok.Getter;

import java.util.Map;

import cn.ggsn.openrxlight.utils.JsonUtils;

/**
 * @ClassName RestReturnValueHandlerAdvice
 * @Description 自定义未受检异常, 会被全局异常捕获
 * @Author zh
 * @Date 2023/04/09 22:25
 * @Version 1.0
 **/
@Getter
public class BizException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -19874907679629349L;
    private Integer code;
    private String message;

    public BizException() {
        super();
    }

    public BizException(Integer code, String message) {
        super(JsonUtils.toJson(Map.of("code", code, "message", message)));
        this.code = code;
        this.message = message;
    }

    public BizException(ErrorCode errorCode, Object... args) {
        this(errorCode.getValue(), errorCode.getFullMessage(args));
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(Map.of("code", code, "message", message));
    }
}
