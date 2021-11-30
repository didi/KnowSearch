package com.didi.arius.gateway.common.enums;

public enum TemplateBlockTypeEnum {
    READ_BLOCK_TYPE(0),
    WRITE_WRITE_TYPE(1);
    int blockType;

    TemplateBlockTypeEnum(int blockType) {
        this.blockType = blockType;
    }

    public int getBlockType() {
        return blockType;
    }
}
