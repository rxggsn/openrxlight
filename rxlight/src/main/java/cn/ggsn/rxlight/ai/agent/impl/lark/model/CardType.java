package cn.ggsn.rxlight.ai.agent.impl.lark.model;

public enum CardType {
    CARD("card"), TEMPLATE("template");

    private final String type;

    CardType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
