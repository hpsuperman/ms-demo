package com.example.ms.common.util;

/**
 * 雪花算法 ID 生成器（Snowflake），生成全局唯一、趋势递增的 64 位 long。
 * 结构：1 位符号位(恒 0) + 41 位时间戳(毫秒,相对 EPOCH) + 5 位数据中心 + 5 位机器 + 12 位序列。
 * 无外部依赖、无网络 IO，单机单毫秒最多 4096 个，适合订单号等唯一标识。
 * 多实例部署时各实例需调用 init() 设置不同的 workerId/datacenterId 避免撞号。
 */
public class SnowflakeIdUtil {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00，起始纪元，越早可用越久

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private static long workerId = 0L;
    private static long datacenterId = 0L;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private SnowflakeIdUtil() {
    }

    /**
     * 初始化机器标识。多实例部署时各实例需不同。workerId/datacenterId 范围 [0, 31]。
     */
    public static synchronized void init(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 越界: " + workerId);
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 越界: " + datacenterId);
        }
        SnowflakeIdUtil.workerId = workerId;
        SnowflakeIdUtil.datacenterId = datacenterId;
    }

    /**
     * 生成下一个 ID，线程安全。时钟回拨时抛异常拒绝生成。
     */
    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("时钟回拨 " + (lastTimestamp - timestamp) + "ms，拒绝生成 ID");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
