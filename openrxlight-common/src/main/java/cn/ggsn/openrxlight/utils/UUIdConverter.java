package cn.ggsn.openrxlight.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class UUIdConverter {
    public static String replaceHyphenWithEmptyChar(UUID id) {
        return id.toString().replaceAll("-", "");
    }

    public static UUID fromHyphenRemovedString(String id) {
        String actualId = StringUtils.joinWith("-",
                id.substring(0, 8),
                id.substring(8, 12),
                id.substring(12, 16),
                id.substring(16, 20),
                id.substring(20));
        return UUID.fromString(actualId);
    }

    public static UUID replaceEmptyWithHyphen(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return fromHyphenRemovedString(id);
    }
}
