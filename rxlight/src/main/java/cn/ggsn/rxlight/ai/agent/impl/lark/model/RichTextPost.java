package cn.ggsn.rxlight.ai.agent.impl.lark.model;

import java.util.List;

import cn.ggsn.openrxlight.lang.Lists2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RichTextPost {
    private String title;
    private List<List<PostContent>> content;

    public void addLine(List<PostContent> line) {
        if (this.content == null) {
            this.content = Lists2.empty();
        }
        this.content.add(line);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostContent {
        private String tag;
        private String text;
    }

    @Getter
    public static class Hyperlink extends PostContent {
        private String href;
        private List<String> style;

        public Hyperlink(String tag, String text, String href, List<String> style) {
            super(tag, text);
            this.href = href;
            this.style = style;
        }
    }

    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        if (content != null) {
            for (List<PostContent> line : content) {
                for (PostContent item : line) {
                    sb.append(item.getText());
                }
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }
}
