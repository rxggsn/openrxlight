package cn.ggsn.openrxlight.httpx;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class RawResponse {
    private int statusCode;
    private String contentType;
    private Map<String, List<String>> headers;
    private byte[] body;
}
