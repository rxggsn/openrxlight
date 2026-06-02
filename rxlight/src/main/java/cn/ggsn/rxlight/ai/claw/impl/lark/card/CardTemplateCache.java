package cn.ggsn.rxlight.ai.claw.impl.lark.card;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardTemplateCache {
    private final Map<String, CardTemplate> templateCache;

    public CardTemplateCache(String path) {
        InputStream source = this.getClass().getResourceAsStream(path);
        List<CardTemplate> cardTemplates = JsonUtils.fromArrayJson(source, CardTemplate.class);
        log.info("Loaded {} card templates from {}", Lists2.size(cardTemplates), path);
        this.templateCache = Maps.newHashMap();
        Lists2.foreach(cardTemplates, template -> this.templateCache.put(template.getTemplateId(), template));
    }

    public CardTemplate getTemplate(String templateId) {
        CardTemplate cardTemplate = this.templateCache.get(templateId);

        if (Objects.isNull(cardTemplate)) {
            throw new IllegalArgumentException(
                    String.format("Card template with id %s not found", templateId));
        }

        return cardTemplate;
    }
}
