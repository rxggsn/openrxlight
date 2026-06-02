package cn.ggsn.openrxlight.model.file;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

public class OpenRxLightFile {
    @Getter
    private String filename;
    @Getter
    private String id;
    private String content;

    @JsonIgnore
    public InputStream getContentStream() {
        return Base64.getDecoder()
                .wrap(new ByteArrayInputStream(this.content
                        .getBytes(StandardCharsets.UTF_8)));
    }
}
