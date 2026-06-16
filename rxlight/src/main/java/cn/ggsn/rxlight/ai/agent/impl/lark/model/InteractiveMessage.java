package cn.ggsn.rxlight.ai.agent.impl.lark.model;

import java.util.List;
import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.lang.Lists2;
import lombok.Getter;

@Getter
public class InteractiveMessage {
    private String title;
    private List<List<Element>> elements;

    @Getter
    public static class Element {
        private String tag;
        private String text;
        private String type;
    }

    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(title)) {
            sb.append("# ").append(title).append("\n");
        }

        Lists2.foreach(this.elements, subElements -> {
            Lists2.foreach(subElements, element -> {
                if ("text".equals(element.getTag())) {
                    sb.append(element.getText()).append("\n");
                }
            });
        });

        return sb.toString().trim();
    }
}
