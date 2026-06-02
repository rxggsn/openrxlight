package cn.ggsn.openrxlight.httpx;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawRequest {
    private String contentType;
    private Map<String, List<String>> headers;
    private Object body;
    private String reqUrl;
    private String httpMethod;

    public void addHeaders(String key, List<String> header) {
        if (headers == null) {
            headers = Maps.newHashMap();
        }
        headers.put(key, header);
    }
}
