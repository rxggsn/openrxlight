package cn.ggsn.openrxlight.services;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.model.file.OpenRxLightFile;
import cn.ggsn.openrxlight.response.OpenRxLightResponse;
import cn.ggsn.openrxlight.utils.Transport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileService {
    private final Config config;

    public OpenRxLightFile download(String fileId) throws Exception {
        OpenRxLightResponse response = Transport.send(this.config, null, "/file/download/" + fileId, "GET", null);
        return response.getBody(OpenRxLightFile.class, this.config);
    }

}
