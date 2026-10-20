package util;

//生成jobid
public class IdUtil {
    // 起始时间戳 (2024-01-01 00:00:00)，可以根据项目启动时间调整
    private final long twepoch = 1704067200000L;
    // 机器ID所占的位数 (5位) + 数据中心ID所占的位数 (5位) = 10位
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    // 支持的最大机器ID和数据中心ID (结果均为 31)
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    // 序列号在ID中所占的位数 (12位)
    private final long sequenceBits = 12L;
    // 机器ID向左移12位
    private final long workerIdShift = sequenceBits;
    // 数据中心ID向左移17位 (12+5)
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间戳向左移22位 (12+5+5)
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    // 生成序列的掩码 (4095)
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public IdUtil(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("Worker ID 不能大于 " + maxWorkerId + " 或小于 0");
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID 不能大于 " + maxDatacenterId + " 或小于 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // 生成下一个唯一ID
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        // 如果当前时间小于上一次生成ID的时间戳，说明系统时钟回拨过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("系统时钟回拨，拒绝生成ID");
        }
        // 如果是同一毫秒内生成，则进行序列号自增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 同一毫秒内的序列号用尽，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒内，序列号重置为0
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        // 通过位运算组合成最终的64位ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    // 阻塞到下一毫秒，直到获得新的时间戳
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}

// 使用雪花算法生成器
//private static final SnowflakeIdGenerator ID_GENERATOR = new SnowflakeIdGenerator(1, 1);
//
//public StandardJob(String actionType, String payload) {
//    this.jobId = String.valueOf(ID_GENERATOR.nextId()); // 生成趋势递增的长整型ID
//    this.submitTimeMillis = TimeUtil.currentMillis();
//    // ...
//}