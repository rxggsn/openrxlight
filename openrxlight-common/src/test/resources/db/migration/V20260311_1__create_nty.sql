CREATE TABLE IF NOT EXISTS nty_message_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- 模板标题
    status SMALLINT NOT NULL,
    -- 当前消息状态：10.新建 20.停用 30.启用 40.等待发送 50.发送中 60.发送成功 70.发送失败
    id_type SMALLINT NOT NULL,
    -- 发送的Id类型：10. userId 20.did 30.手机号 40.openId 50.email 60.企业微信userId
    channel_config_id BIGINT NOT NULL,
    -- 关联的通道配置账号
    shield_type SMALLINT NOT NULL,
    -- 屏蔽类型：10.夜间不屏蔽 20.夜间屏蔽 30.夜间屏蔽(次日早上9点发送)
    crontab VARCHAR(255),
    -- 推送消息的时间, null：立即发送, else：crontab 表达式
    content TEXT,
    -- 消息内容 {} 为占位符
    account_id UUID,
    -- 账号ID, null表示通用模板，支持所有账号使用
    account_type SMALLINT NOT NULL,
    -- 目标账号类型
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    -- 是否删除
    scene_type SMALLINT NOT NULL -- 场景类型
);
create sequence nty_message_template_SEQ start with 1 increment by 1;
COMMENT ON TABLE nty_message_template IS 'Notification message template';
COMMENT ON COLUMN nty_message_template.id IS 'Primary key';
COMMENT ON COLUMN nty_message_template.name IS '模板标题';
COMMENT ON COLUMN nty_message_template.status IS '当前消息状态：10.新建 20.停用 30.启用 40.等待发送 50.发送中 60.发送成功 70.发送失败';
COMMENT ON COLUMN nty_message_template.id_type IS '发送的Id类型：10. userId 20.did 30.手机号 40.openId 50.email 60.企业微信userId';
COMMENT ON COLUMN nty_message_template.channel_config_id IS '关联的通道配置账号';
COMMENT ON COLUMN nty_message_template.shield_type IS '屏蔽类型：10.夜间不屏蔽 20.夜间屏蔽 30.夜间屏蔽(次日早上9点发送)';
COMMENT ON COLUMN nty_message_template.crontab IS '推送消息的时间, null：立即发送, else：crontab 表达式';
COMMENT ON COLUMN nty_message_template.content IS '消息内容 {} 为占位符';
COMMENT ON COLUMN nty_message_template.account_id IS '账号ID, null表示通用模板，支持所有账号使用';
COMMENT ON COLUMN nty_message_template.account_type IS '目标账号类型';
COMMENT ON COLUMN nty_message_template.deleted IS '是否删除';
COMMENT ON COLUMN nty_message_template.scene_type IS '场景类型';
CREATE TABLE IF NOT EXISTS nty_channel_configuration (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- 渠道名称
    type INTEGER NOT NULL,
    -- 渠道类型，对应 ChannelType.code
    accounts JSONB NULL,
    -- 渠道账号信息，存储为 JSON 数组，不同渠道类型对应不同的账号信息，具体结构由 ChannelConfiguration.ChannelAccount 定义
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    -- 是否删除
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- 创建时间
);
create sequence nty_channel_configuration_SEQ start with 1 increment by 1;
COMMENT ON TABLE nty_channel_configuration IS '渠道账号信息';
COMMENT ON COLUMN nty_channel_configuration.id IS '主键';
COMMENT ON COLUMN nty_channel_configuration.name IS '渠道名称';
COMMENT ON COLUMN nty_channel_configuration.type IS '渠道类型，对应 ChannelType.code';
COMMENT ON COLUMN nty_channel_configuration.accounts IS '渠道账号信息，存储为 JSON 数组，不同渠道类型对应不同的账号信息，具体结构由 ChannelConfiguration.ChannelAccount 定义';
COMMENT ON COLUMN nty_channel_configuration.deleted IS '是否删除';
COMMENT ON COLUMN nty_channel_configuration.created_time IS '创建时间';
CREATE TABLE IF NOT EXISTS nty_message_record (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    -- 消息模板Id
    content TEXT NOT NULL,
    -- 信息发送的内容
    response TEXT,
    -- 回执信息
    status INTEGER NOT NULL,
    -- 发送状态，10.发送 20.成功 30.失败
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 创建时间
    account_id UUID NOT NULL -- 账号ID
);
create sequence nty_message_record_SEQ start with 1 increment by 1;
COMMENT ON TABLE nty_message_record IS '发送信息（回执和发送记录）';
COMMENT ON COLUMN nty_message_record.id IS '主键';
COMMENT ON COLUMN nty_message_record.template_id IS '消息模板Id';
COMMENT ON COLUMN nty_message_record.content IS '信息发送的内容';
COMMENT ON COLUMN nty_message_record.response IS '回执信息';
COMMENT ON COLUMN nty_message_record.status IS '发送状态，10.发送 20.成功 30.失败';
COMMENT ON COLUMN nty_message_record.created_time IS '创建时间';
COMMENT ON COLUMN nty_message_record.account_id IS '账号ID';