package cn.ggsn.openrxlight.event;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;

import cn.ggsn.openrxlight.lang.ProtobufHelper;
import cn.ggsn.openrxlight.utils.JsonUtils;
import kotlin.TypeCastException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName CloudEvent
 * @Author Jason.Song
 * @Description CloudEvent 1.0 Protocol Implementation, see
 *              https://github.com/cloudevents/spec
 * @Date 2024/6/12 10:45
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@Builder(access = lombok.AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CloudEvent<T> {
    public static final String PUB_SCHEMA = "publish";

    private String specversion; // 规范版本
    private String id; // 事件ID
    private String source; // 事件来源
    private String type; // 事件类型
    private String time; // timestamp when the event occurred
    private T data; // event's data
    private String datacontenttype; // event's data content type, default to
                                    // application/json
    private String subject; // 事件主题, Topic
    private String dataschema; // 事件数据模式

    public CloudEvent(String id, String source, String type, String subject, ContentType contentType) {
        this.specversion = "1.0";
        this.datacontenttype = contentType.getType();
        this.time = LocalDateTime
                .now()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.id = id;
        this.source = source;
        this.type = type;
        this.subject = subject;
        this.dataschema = PUB_SCHEMA;
    }

    @SuppressWarnings("unchecked")
    public static <T> CloudEvent<T> parse(Map<String, String> body, Class<T> dataType) {
        CloudEvent<T> event = CloudEvent.<T>builder()
                .specversion((String) body.get("specversion"))
                .id((String) body.get("id"))
                .source((String) body.get("source"))
                .type((String) body.get("type"))
                .time((String) body.get("time"))
                .datacontenttype((String) body.get("datacontenttype"))
                .subject((String) body.get("subject"))
                .dataschema((String) body.get("dataschema"))
                .build();
        String dataStr = body.get("data");
        ContentType contentType = ContentType.fromString(event.getDatacontenttype());
        switch (contentType) {
            case TEXT_PLAIN:
                if (dataType == String.class) {
                    event.setData((T) dataStr);
                } else {
                    throw new TypeCastException("data is not text/plain content type");
                }
                break;
            case BIN_PROTOBUF:
                if (ProtobufHelper.isProtobufClass(dataType)) {
                    byte[] decoded = Base64.getDecoder().decode(dataStr.getBytes(StandardCharsets.UTF_8));

                    event.setData((T) ProtobufHelper.unmarshal(decoded, (Class<? extends Message>) dataType));
                    break;
                } else {
                    throw new TypeCastException("data is not binary/protobuf content type");
                }
            case APPLICATION_JSON:
                event.setData(JsonUtils.fromJson(dataStr, dataType));
                break;
            default:
                break;
        }

        return event;
    }

    public Map<String, String> toMap() {
        Map<String, String> result = Maps.newHashMap();
        result.put("specversion", this.specversion);
        result.put("id", this.id);
        result.put("source", this.source);
        result.put("type", this.type);
        result.put("time", this.time);
        result.put("datacontenttype", this.datacontenttype);
        result.put("subject", this.subject);
        result.put("dataschema", this.dataschema);
        ContentType contentType = ContentType.fromString(this.datacontenttype);
        switch (contentType) {
            case APPLICATION_JSON:
                result.put("data", JsonUtils.toJson(this.data));
                break;
            case TEXT_PLAIN:
                if (this.data instanceof String) {
                    result.put("data", (String) this.data);
                } else {
                    throw new TypeCastException("data is not text/plain content type");
                }
            case BIN_PROTOBUF:
                if (ProtobufHelper.isProtobuf(this.data)) {
                    result.put("data",
                            Base64.getEncoder().encodeToString(ProtobufHelper.marshal((Message) this.data)));
                } else {
                    throw new TypeCastException("data is not binary/protobuf content type");
                }
            default:
                break;
        }

        return result;
    }

    public String toJson() {
        return JsonUtils.toJson(this.toMap());
    }

    @SuppressWarnings("unchecked")
    public static <T> CloudEvent<T> parseString(String value, Class<T> dataType) {
        var body = JsonUtils.toJsonNode(value);

        return (CloudEvent<T>) CloudEvent.builder()
                .datacontenttype(body.get("datacontenttype").asText())
                .dataschema(body.get("dataschema").asText())
                .id(body.get("id").asText())
                .source(body.get("source").asText())
                .specversion(body.get("specversion").asText())
                .subject(body.get("subject").asText())
                .time(body.get("time").asText())
                .type(body.get("type").asText())
                .data(CloudEvent.parseBody(Objects.requireNonNull(body.get("datacontenttype").asText()),
                        Objects.requireNonNull(body.get("data")), Objects.requireNonNull(dataType)))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T parseBody(@Nonnull String contentType, @Nonnull JsonNode data, @Nonnull Class<T> dataType) {
        var contentTyp = ContentType.fromString(contentType);
        switch (contentTyp) {
            case APPLICATION_JSON:
                return JsonUtils.fromJson(data.asText(), dataType);
            case TEXT_PLAIN:
                if (dataType == String.class) {
                    return (T) data.asText();
                } else {
                    throw new IllegalArgumentException("Data type must be String for text/plain content type");
                }
            case BIN_PROTOBUF:
                if (dataType.isAssignableFrom(Message.class) && data.isTextual()) {
                    byte[] decoded = Base64.getDecoder().decode(data.asText());
                    return (T) ProtobufHelper.unmarshal(decoded, (Class<? extends Message>) dataType);
                }

                throw new IllegalArgumentException("Data type must be valid protobuf message generated by protoc cli");
            default:
                throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
    }

    @JsonIgnore
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(this.time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @JsonIgnore
    public String getTopic(EventBusType eventBusType) {
        switch (eventBusType) {
            case NATS:
                if (StringUtils.isNotBlank(this.id)) {
                    return this.subject + "." + this.id;
                } else {
                    return this.subject;
                }
            default:
                return this.subject;
        }
    }

}
