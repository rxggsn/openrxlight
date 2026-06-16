package cn.ggsn.rxlight.ai.claw.impl.lark.card;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.ai.agent.impl.lark.card.CardTemplate;

@RunWith(org.junit.runners.JUnit4.class)
public class CardTemplateTest {
        @Test
        public void testReplaceVariables() {
                String json = """
                                {
                                    "template_id": "template_123",
                                    "stream_key": "stream_123",
                                    "content": {
                                        "type": "text",
                                        "text": "${var1}",
                                        "options": "${arr1}",
                                        "objoptions": [
                                            {
                                                "name": "Option1",
                                                "value": "${var1}"
                                            },
                                            {
                                                "name": "Option2",
                                                "value": "${var2}"
                                            }
                                        ],
                                        "objoptionswhole": "${objarr1}"
                                    }
                                }
                                """;
                CardTemplate template = JsonUtils.fromJson(json, CardTemplate.class);
                org.junit.Assert.assertNotNull(template);
                template.replaceVariables(Map.of("var1", JsonNodeFactory.instance.textNode("Hello, World!"), "var2",
                                JsonNodeFactory.instance.textNode("Hello, World Var2!"), "arr1",
                                JsonNodeFactory.instance.arrayNode().add("Option1").add("Option2"), "objarr1",
                                JsonNodeFactory.instance.arrayNode()
                                                .add(JsonNodeFactory.instance.objectNode().put("name", "Option1")
                                                                .put("value", "Hello, World!"))
                                                .add(JsonNodeFactory.instance.objectNode().put("name", "Option2").put(
                                                                "value",
                                                                "Hello, World Var2!"))),
                                null);
                org.junit.Assert.assertEquals("Hello, World!", template.getContent().get("text").asText());

                org.junit.Assert.assertTrue(template.getContent().get("options").isArray());
                org.junit.Assert.assertEquals(2, template.getContent().get("options").size());
                org.junit.Assert.assertEquals("Option1", template.getContent().get("options").get(0).asText());
                org.junit.Assert.assertEquals("Option2", template.getContent().get("options").get(1).asText());

                org.junit.Assert.assertTrue(template.getContent().get("objoptions").isArray());
                org.junit.Assert.assertEquals(2, template.getContent().get("objoptions").size());
                org.junit.Assert.assertEquals("Hello, World!",
                                template.getContent().get("objoptions").get(0).get("value").asText());
                org.junit.Assert.assertEquals("Hello, World Var2!",
                                template.getContent().get("objoptions").get(1).get("value").asText());

                org.junit.Assert.assertTrue(template.getContent().get("objoptionswhole").isArray());
                org.junit.Assert.assertEquals(2, template.getContent().get("objoptionswhole").size());
                org.junit.Assert.assertEquals("Option1",
                                template.getContent().get("objoptionswhole").get(0).get("name").asText());
                org.junit.Assert.assertEquals("Hello, World!",
                                template.getContent().get("objoptionswhole").get(0).get("value").asText());
                org.junit.Assert.assertEquals("Option2",
                                template.getContent().get("objoptionswhole").get(1).get("name").asText());
                org.junit.Assert.assertEquals("Hello, World Var2!",
                                template.getContent().get("objoptionswhole").get(1).get("value").asText());
        }
}
