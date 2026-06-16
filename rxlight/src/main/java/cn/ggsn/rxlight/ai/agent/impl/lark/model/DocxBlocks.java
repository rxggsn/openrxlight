package cn.ggsn.rxlight.ai.agent.impl.lark.model;

import com.lark.oapi.service.docx.v1.model.Block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocxBlocks {
    private Block[] blocks;
    private String[] firstLevelBlockIds;
}
