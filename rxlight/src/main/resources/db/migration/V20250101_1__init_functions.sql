CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE OR REPLACE FUNCTION uuid_generate_v7()
RETURNS uuid
AS $$
DECLARE
    unix_ts_ms bytea;
    uuid_bytes bytea;
BEGIN
    -- 1. 获取当前毫秒级时间戳 (6 字节)
    unix_ts_ms := decode(lpad(to_hex(floor(extract(epoch from clock_timestamp()) * 1000)::bigint), 12, '0'), 'hex');

    -- 2. 生成 10 字节的随机数
    uuid_bytes := unix_ts_ms || gen_random_bytes(10);

    -- 3. 设置版本号 (4) 和变体号 (8)
    -- 第 7 个字节的高 4 位设为 0111 (v7)
    uuid_bytes := set_byte(uuid_bytes, 6, (get_byte(uuid_bytes, 6) & 15) | 112);
    -- 第 9 个字节的高 2 位设为 10 (variant 1)
    uuid_bytes := set_byte(uuid_bytes, 8, (get_byte(uuid_bytes, 8) & 63) | 128);

    RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$ LANGUAGE plpgsql VOLATILE;

CREATE SEQUENCE IF NOT EXISTS snowflake_id_seq 
    MINVALUE 0 
    MAXVALUE 4095 
    CYCLE;
CREATE OR REPLACE FUNCTION next_snowflake_id()
RETURNS bigint AS $$
DECLARE
    -- 1. 配置参数
    our_epoch bigint := 1704067200000; -- 初始偏移时间戳 (例如 2024-01-01 00:00:00 UTC)
    worker_id int := 1;               -- 当前数据库节点的机器 ID (0-1023)
    
    -- 2. 变量定义
    now_ms bigint;
    seq_id bigint;
    result bigint;
BEGIN
    -- 获取当前毫秒时间戳
    SELECT floor(extract(epoch from clock_timestamp()) * 1000)::bigint INTO now_ms;
    
    -- 获取序列号并自增
    SELECT nextval('snowflake_id_seq') INTO seq_id;

    -- 3. 位运算拼接
    -- 时间戳偏移 22 位 | 机器 ID 偏移 12 位 | 序列号
    result := (now_ms - our_epoch) << 22;
    result := result | (worker_id << 12);
    result := result | (seq_id);

    RETURN result;
END;
$$ LANGUAGE plpgsql VOLATILE;