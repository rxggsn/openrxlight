package cn.ggsn.openrxlight.utils;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class JsonUtilsTest {
    @Test
    public void testToJson() {
        var node = JsonNodeFactory.instance.textNode("text");

        assertEquals(JsonUtils.toJson(node), "text");

        var objnode = JsonNodeFactory.instance.objectNode();
        objnode.put("name", "name2222");
        objnode.put("id", "123");

        assertEquals(JsonUtils.toJson(objnode), JsonUtils.toJson(Map.of("name", "name2222", "id", "123")));
    }
}
