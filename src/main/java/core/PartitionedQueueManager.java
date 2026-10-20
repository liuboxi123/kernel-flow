package core;

import Job.Job;
import core.FlowQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PartitionedQueueManager {
    private static final int PARTITION_COUNT = 16;
    private final FlowQueue[] partitions;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public PartitionedQueueManager() {
        this.partitions = new FlowQueue[PARTITION_COUNT];
        for (int i = 0; i < PARTITION_COUNT; i++) {
            this.partitions[i] = FlowQueue.getPartition(i);
        }
    }

    public boolean offer(Job job) {
        if (job == null) {
            return false;
        }
        FlowQueue partition = FlowQueue.getPartition(job.getJobId());
        return partition.offer(job);
    }

    public Job takeJobFromAny() throws InterruptedException {
        while (true) {
            int startIndex = roundRobinIndex.getAndIncrement() % PARTITION_COUNT;

            for (int i = 0; i < PARTITION_COUNT; i++) {
                int index = (startIndex + i) % PARTITION_COUNT;
                FlowQueue partition = partitions[index];

                if (!partition.isEmpty()) {
                    try {
                        Job job = partition.takeJob();
                        if (job != null) {
                            return job;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }

            synchronized (this) {
                wait(10);
            }
        }
    }

    public Job peek() {
        for (FlowQueue partition : partitions) {
            Job job = partition.peek();
            if (job != null) {
                return job;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        for (FlowQueue partition : partitions) {
            if (!partition.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        int totalSize = 0;
        for (FlowQueue partition : partitions) {
            totalSize += partition.size();
        }
        return totalSize;
    }

    public void clear() {
        for (FlowQueue partition : partitions) {
            partition.clear();
        }
    }

    public Job getJobById(long jobId) {
        FlowQueue partition = FlowQueue.getPartition(jobId);
        return partition.getJobById(jobId);
    }

    public List<Job> getAllJobs() {
        List<Job> allJobs = new ArrayList<>();
        for (FlowQueue partition : partitions) {
            // 这里需要添加一个方法来获取所有任务
        }
        return allJobs;
    }
}
