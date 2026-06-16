package cn.ggsn.openrxlight.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.EnumNamingStrategies;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.MonthDayDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.MonthDaySerializer;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JsonUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        OBJECT_MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        OBJECT_MAPPER.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
        OBJECT_MAPPER.setEnumNamingStrategy(EnumNamingStrategies.SNAKE_CASE);
        // OBJECT_MAPPER.enable(SerializationFeature.WRAP_ROOT_VALUE);
        // OBJECT_MAPPER.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));
        module.addSerializer(MonthDay.class, new MonthDaySerializer(DateTimeFormatter.ofPattern("MM-dd")));
        module.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME));
        module.addDeserializer(MonthDay.class, new MonthDayDeserializer(DateTimeFormatter.ofPattern("MM-dd")));

        OBJECT_MAPPER.registerModule(module);
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> String toJson(T object) {
        return toJson(object, OBJECT_MAPPER);
    }

    public static <T> String toJson(T object, ObjectMapper objectMapper) {
        try {
            if (object instanceof JsonNode) {
                if (((JsonNode) object).isTextual()) {
                    return ((JsonNode) object).asText();
                } else if (((JsonNode) object).isBoolean()) {
                    return Boolean.toString(((JsonNode) object).asBoolean());
                } else {
                    return ((JsonNode) object).toString();
                }
            }
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing object", e);
            return "{}";
        }
    }

    public static JsonNode toJsonNode(String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (IOException e) {
            log.debug("parse json string error:" + value, e);
            return null;
        }
    }

    public static <T> JsonNode toJsonNode(T value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    public static JsonNode toJsonNode(InputStream stream) {
        try {
            return OBJECT_MAPPER.readTree(stream);
        } catch (IOException e) {
            log.debug("parse json string error:" + stream, e);
            return null;
        }
    }

    public static <T> T fromJson(JsonNode jsonNode, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.convertValue(jsonNode, clazz);
        } catch (IllegalArgumentException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JsonUtils.fromJson(json, clazz, OBJECT_MAPPER);
    }

    public static <T> T fromJson(String json, Class<T> clazz, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static <T> T fromJson(InputStream stream, Class<T> clazz) {
        return fromJson(stream, clazz, OBJECT_MAPPER);
    }

    public static <T> T fromJson(InputStream stream, Class<T> clazz, ObjectMapper objectMapper) {
        if (Objects.isNull(stream)) {
            return null;
        }
        try {
            return objectMapper.readValue(stream, clazz);
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static <T> T fromJson(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static <T> List<T> fromArrayJson(String json, Class<T> tClass) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, tClass));
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static <T> List<T> fromArrayJson(byte[] jsonBytes, Class<T> tClass) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, tClass));
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static <T> List<T> fromArrayJson(JsonNode stream, Class<T> tClass) {
        if (stream.isArray() && stream.size() > 0) {
            return Lists.newArrayList(Objects.requireNonNull(stream.elements())).stream()
                    .map(node -> fromJson(node, tClass))
                    .collect(Collectors.toList());
        }

        return null;
    }

    public static <T> List<T> fromArrayJson(InputStream json, Class<T> tClass) {
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, tClass));
        } catch (IOException e) {
            log.error("Error while parsing json", e);
            return null;
        }
    }

    public static JsonNode mergeInto(JsonNode target, JsonNode base) {
        if (target == null || target.isNull()) {
            return base;
        }
        if (base == null || base.isNull()) {
            return target;
        }
        if (target.isObject() && base.isObject()) {
            Iterator<String> fieldNames = base.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode jsonNode = target.get(fieldName);
                // if field exists and is an embedded object
                if (jsonNode != null && jsonNode.isObject()) {
                    mergeInto(jsonNode, base.get(fieldName));
                } else {
                    if (target instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                        // Overwrite field
                        ((com.fasterxml.jackson.databind.node.ObjectNode) target).set(fieldName, base.get(fieldName));
                    }
                }
            }
            return target;
        } else if (target.isArray() && base.isArray()) {
            List<JsonNode> merged = new ArrayList<>();
            target.forEach(merged::add);
            base.forEach(merged::add);
            return JsonNodeFactory.instance.arrayNode().addAll(merged);
        } else if (target.isTextual() && base.isTextual()) {
            // concatenate strings
            return JsonNodeFactory.instance.textNode(target.asText() + base.asText());
        } else if (base.isNull()) {
            return target;
        } else {
            return base;
        }
    }

    public static <T> Map<String, Object> toMap(T obj) {
        if (obj == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        Class<?> clazz = obj.getClass();

        NamingBase strategy = Optional.ofNullable(obj.getClass().getAnnotation(JsonNaming.class))
                .flatMap(naming -> {
                    try {
                        return Optional.of((NamingBase) naming.value().getDeclaredConstructor().newInstance());
                    } catch (Exception e) {
                        log.error("Error creating naming strategy instance", e);
                        return Optional.empty();
                    }
                })
                .orElseGet(() -> new com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCamelCaseStrategy());

        while (clazz != null && clazz != Object.class) {
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                try {
                    result.put(strategy.translate(field.getName()), toJsonNode(field.get(obj)));
                } catch (IllegalAccessException e) {
                    log.error("Error accessing field: " + field.getName(), e);
                }
            });
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        map.forEach((key, val) -> {
            objectNode.replace(key, toJsonNode(val));
        });
        return fromJson(objectNode, clazz);
    }

    public static <T> T fromMapJsonNode(Map<String, JsonNode> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        map.forEach((key, val) -> {
            objectNode.replace(key, val);
        });
        return fromJson(objectNode, clazz);
    }
}
