package com.gzy.pestdetectionsystem.utils;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 *
 * 雪花算法生成的ID结构：
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位符号位 + 41位时间戳 + 5位数据中心ID + 5位机器ID + 12位序列号 = 64位
 *
 */
@Component
public class SnowflakeIdGenerator {

    //起始时间戳
    private static final long START_TIMESTAMP = 1768311134169L;

    //数据中心ID位数
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long DATACENTER_ID = 1L;

    //机器ID位数
    private static final long MACHINE_ID_BITS = 5L;
    private static final long MACHINE_ID = 1L;

    // 序列号
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);


    //左移位数
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATACENTER_ID_BITS;

    //每毫秒4096个ID
    private long sequence = 0L;
    /**
     记录上一个id生成时的时间戳
     当前生成 ID 是否同一毫秒 → sequence 自增
     */
    private long lastTimestamp = -1L;

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("时钟回退，拒绝生成ID %d 毫秒", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;//sequence = (sequence + 1) % 4096;
            if (sequence == 0L) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        }else{
            sequence = 0L;
        }
        //更新上次生成时间戳
        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (DATACENTER_ID << DATACENTER_SHIFT)
                | (MACHINE_ID << MACHINE_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
