package cn.ggsn.rxlight.transaction.pay.fuyou.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.rxlight.transaction.pay.fuyou.Consts;
import cn.ggsn.rxlight.transaction.pay.fuyou.JsonSignature;
import cn.ggsn.rxlight.transaction.pay.fuyou.SignatureIgnore;
import cn.ggsn.openrxlight.utils.BeanUtils;
import cn.ggsn.openrxlight.utils.JsonUtils;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class FuyouSignatureKeyLink {
    protected String linkVariables() {
        Map<String, Object> objectMap = Maps2.empty();
        StringBuilder sb = new StringBuilder();
        List<Field> fields = BeanUtils.getAllFields(this.getClass());
        Lists2.foreach(fields, field -> {
            field.setAccessible(true);
            try {
                JsonProperty annotation = field.getAnnotation(JsonProperty.class);
                JsonIgnore ignore = field.getAnnotation(JsonIgnore.class);
                SignatureIgnore signatureIgnore = field.getAnnotation(SignatureIgnore.class);
                JsonSignature jsonSignature = field.getAnnotation(JsonSignature.class);
                if (ignore == null && signatureIgnore == null) {
                    String key;
                    if (annotation != null) {
                        key = annotation.value();
                    } else {
                        key = field.getName();
                    }

                    if (jsonSignature != null) {
                        objectMap.put(key, JsonUtils.toJson(field.get(this)));
                    } else {
                        objectMap.put(key, field.get(this));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        if (objectMap.isEmpty()) {
            return "";
        }
        @SuppressWarnings("null")
        List<String> keys = Lists.newArrayList(objectMap.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            if (!StringUtils.equals("sign", key) && needSignCheck(key)) {
                sb.append(key)
                        .append("=")
                        .append(objectMap.get(key) == null ? "" : StringUtils.trim(String.valueOf(objectMap.get(key))))
                        .append("&");
            }
        }

        if (sb.length() != 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public boolean needSignCheck(String key) {
        return !Consts.FIELD_SKIP_SIGN_CHECK_PATTERN.matcher(key).find();
    }

}
