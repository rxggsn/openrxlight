package cn.ggsn.openrxlight.lang;

// import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Map;
// import java.util.Map;
import java.util.Objects;
// import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.TypeMismatchException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Maps;
import com.google.protobuf.Any;
import com.google.protobuf.CodedOutputStream;
// import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
// import com.google.protobuf.Parser;
import com.google.protobuf.Value;

public class ProtobufHelper {
    // private static Map<Class<?>, Parser<?>> parserMap = new
    // ConcurrentHashMap<>();

    public static <T extends Message> T unmarshal(byte[] data, Class<T> clazz) {
        try {
            Any any = Any.parseFrom(data);

            if (any.is(clazz)) {
                return any.unpack(clazz);
            } else {
                throw new TypeMismatchException("fail to unmarshal protobuf data: type mismatch");
            }
        } catch (Exception e) {
            throw new RuntimeException("fail to unmarshal protobuf data", e);
        }
    }

    // @SuppressWarnings("unchecked")
    // private static <T> Parser<T> getParser(Class<T> clazz) {
    // if (parserMap.containsKey(clazz)) {
    // return (Parser<T>) parserMap.get(clazz);
    // }
    // try {
    // T message;
    // Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
    // declaredConstructor.setAccessible(true);
    // message = declaredConstructor.newInstance();
    // Parser<T> parser = (Parser<T>) ((GeneratedMessage)
    // message).getParserForType();
    // parserMap.put(clazz, parser);
    // return parser;
    // } catch (Exception e) {
    // throw new RuntimeException("fail to get protobuf parser", e);
    // }
    // }

    public static <T> boolean isProtobuf(T data) {
        if (Objects.isNull(data)) {
            return false;
        }
        return Message.class.isAssignableFrom(data.getClass());
    }

    public static boolean isProtobufClass(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return false;
        }
        return Message.class.isAssignableFrom(clazz);
    }

    public static <T extends Message> byte[] marshal(T data) {
        if (Objects.isNull(data)) {
            return null;
        }
        try {
            ByteBuffer buffer = ByteBuffer.allocate(data.getSerializedSize());
            CodedOutputStream stream = CodedOutputStream.newInstance(buffer);
            data.writeTo(stream);
            stream.flush();
            return buffer.flip().array();
        } catch (Exception e) {
            throw new RuntimeException("fail to unmarshal protobuf data", e);
        }
    }

    public static JsonNode toJsonNode(Value value) {
        if (Objects.isNull(value)) {
            return null;
        }

        switch (value.getKindCase()) {
            case NULL_VALUE:
                return null;
            case BOOL_VALUE:
                return BooleanNode.valueOf(value.getBoolValue());
            case NUMBER_VALUE:
                return DoubleNode.valueOf(value.getNumberValue());
            case STRING_VALUE:
                return TextNode.valueOf(value.getStringValue());
            case STRUCT_VALUE:
                ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                value.getStructValue().getFieldsMap().forEach((k, v) -> {
                    objectNode.set(k, toJsonNode(v));
                });
                return objectNode;
            case LIST_VALUE:
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                value.getListValue().getValuesList().forEach(v -> {
                    arrayNode.add(toJsonNode(v));
                });
                return arrayNode;
            default:
                return null;
        }
    }

    public static Map<String, JsonNode> toMap(Struct structValue) {
        if (Objects.isNull(structValue)) {
            return null;
        }
        Map<String, JsonNode> map = Maps.newHashMap();
        structValue.getFieldsMap().forEach((k, v) -> {
            map.put(k, toJsonNode(v));
        });
        return map;
    }

    public static Value toValue(JsonNode node) {
        if (Objects.isNull(node) || node.isNull()) {
            return Value.newBuilder().setNullValue(com.google.protobuf.NullValue.NULL_VALUE).build();
        }

        if (node.isBoolean()) {
            return Value.newBuilder().setBoolValue(node.asBoolean()).build();
        }

        if (node.isNumber()) {
            return Value.newBuilder().setNumberValue(node.asDouble()).build();
        }

        if (node.isTextual()) {
            return Value.newBuilder().setStringValue(node.asText()).build();
        }

        if (node.isObject()) {
            Struct.Builder structBuilder = Struct.newBuilder();
            node.properties().forEach(entry -> {
                structBuilder.putFields(entry.getKey(), toValue(entry.getValue()));
            });
            return Value.newBuilder().setStructValue(structBuilder).build();
        }

        if (node.isArray()) {
            com.google.protobuf.ListValue.Builder listBuilder = com.google.protobuf.ListValue.newBuilder();
            node.forEach(element -> {
                listBuilder.addValues(toValue(element));
            });
            return Value.newBuilder().setListValue(listBuilder).build();
        }

        return Value.newBuilder().setNullValue(com.google.protobuf.NullValue.NULL_VALUE).build();
    }

}