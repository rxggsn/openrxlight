package cn.ggsn.openrxlight.lang;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class PlaceHolderUtils {
    public static String replacePlaceholders(String text, Map<String, String> values) {
        if (StringUtils.isEmpty(text) || Maps2.isEmpty(values)) {
            return text;
        }
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
        Properties properties = new Properties();
        values.forEach((key, value) -> {
            if (value != null) {
                properties.put(key, value);
            }
        });
        return helper.replacePlaceholders(text, properties);
    }
}
