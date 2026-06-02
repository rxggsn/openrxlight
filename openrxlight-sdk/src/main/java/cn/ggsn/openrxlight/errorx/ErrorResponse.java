package cn.ggsn.openrxlight.errorx;

import java.util.Map;

import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse extends RuntimeException {
    private Integer code;
    private String message;

    @Override
    public String toString() {
        return JsonUtils.toJson(Map.of("code", this.code, "message", this.message));
    }
}
