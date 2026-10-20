package core;

import Job.Job;
import priority.DynamicPriority;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FlowQueue {
    private static final int PARTITION_COUNT = 16;
    private static final FlowQueue[] partitions = new FlowQueue[PARTITION_COUNT];
    private static int AGING_TRIGGER_THRESHOLD = 10;    // 堆更新检查的触发阈值
    private int jobProcessedCount = 0;

    static {
        for (int i = 0; i < PARTITION_COUNT; i++) {
            partitions[i] = new FlowQueue();
        }
    }
    
    private final List<Job> heap;
    private final Map<Long, Integer> indexMap;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private static final long AGING_CHECK_INTERVAL_MS = 1000;
    private volatile long lastAgingTime = System.currentTimeMillis();
    private final DynamicPriority dynamicPriority = new DynamicPriority();
    
    private FlowQueue() {
        this.heap = new ArrayList<>();
        this.indexMap = new HashMap<>();
    }
    
    private static int getPartitionIndex(long jobId) {
        return (int) (Math.abs(jobId) % PARTITION_COUNT);
    }
    
    public static FlowQueue getPartition(long jobId) {
        return partitions[getPartitionIndex(jobId)];
    }
    
    public static FlowQueue[] getAllPartitions() {
        return partitions;
    }

    public void updatePriority(Job element) {
        lock.lock();
        try {
            doUpdatePriority(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    private void doUpdatePriority(Job element) {
        Integer idx = indexMap.get(element.getJobId());
        if (idx != null) {
            Job oldJob = heap.get(idx);
            int oldPri = oldJob.getEffectivePriority();
            int newPri = element.getEffectivePriority();

            heap.set(idx, element);

            if (newPri < oldPri) {
                siftUp(idx, element);
            } else if (newPri > oldPri) {
                siftDown(idx, element);
            }
        } else {
            heap.add(element);
            int idx_new = heap.size() - 1;
            indexMap.put(element.getJobId(), idx_new);
            siftUp(idx_new, element);
        }
    }

    public Job takeJob() throws InterruptedException {
        lock.lock();
        try {
            while(heap.isEmpty()){
                notEmpty.await();
            }

            jobProcessedCount++;
            if(jobProcessedCount >= AGING_TRIGGER_THRESHOLD){
                applyAgingIfNeededInternal();   // 每提交10次任务更新优先级
                jobProcessedCount = 0;  // 重置
            }
            
            if (heap.isEmpty()) {
                return null;
            }
            
            if (heap.size() == 1) {
                Job result = heap.remove(0);
                indexMap.clear();
                return result;
            }
            
            Job result = heap.get(0);
            Job last = heap.remove(heap.size() - 1);
            heap.set(0, last);
            indexMap.put(last.getJobId(), 0);
            siftDown(0, last);
            indexMap.remove(result.getJobId());
            return result;
        } finally {
            lock.unlock();
        }
    }

    public Job peek() {
        lock.lock();
        try {
            return heap.isEmpty() ? null : heap.get(0);
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return heap.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return heap.size();
        } finally {
            lock.unlock();
        }
    }

    private void siftUp(int k, Job key) {
        int keyPriority = key.getEffectivePriority();
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Job parentVal = heap.get(parent);
            if (keyPriority >= parentVal.getEffectivePriority()) break;
            heap.set(k, parentVal);
            indexMap.put(parentVal.getJobId(), k);
            k = parent;
        }
        heap.set(k, key);
        indexMap.put(key.getJobId(), k);
    }

    private void siftDown(int k, Job key) {
        int keyPriority = key.getEffectivePriority();
        int half = heap.size() >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Job leftChild = heap.get(child);
            
            if (child + 1 < heap.size()) {
                Job rightChild = heap.get(child + 1);
                if (rightChild.getEffectivePriority() < leftChild.getEffectivePriority()) {
                    child++;
                    leftChild = rightChild;
                }
            }
            
            if (keyPriority <= leftChild.getEffectivePriority()) break;
            heap.set(k, leftChild);
            indexMap.put(leftChild.getJobId(), k);
            k = child;
        }
        heap.set(k, key);
        indexMap.put(key.getJobId(), k);
    }

    public boolean offer(Job job) {
        if (job == null) {
            return false;
        }
        updatePriority(job);
        return true;
    }

    public void clear() {
        lock.lock();
        try {
            heap.clear();
            indexMap.clear();
        } finally {
            lock.unlock();
        }
    }
    
    public Job getJobById(long jobId) {
        lock.lock();
        try {
            Integer index = indexMap.get(jobId);
            return (index != null && index < heap.size()) ? heap.get(index) : null;
        } finally {
            lock.unlock();
        }
    }

    private void applyAgingIfNeededInternal() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAgingTime < AGING_CHECK_INTERVAL_MS) {
            return;
        }
        lastAgingTime = currentTime;

        Set<Long> toRemoveIds = new HashSet<>();
        int size = heap.size();
        
        for (int i = 0; i < size; i++) {
            Job job = heap.get(i);
            long endTime = job.getSubmitTime() + job.getDeadline();
            if (currentTime > endTime && job.getStatus() == Job.JobStatus.PENDING) {
                job.setStatus(Job.JobStatus.DEAD);
                toRemoveIds.add(job.getJobId());
                continue;
            }
            
            dynamicPriority.aging(job);
        }
        
        if (!toRemoveIds.isEmpty()) {
            batchRemoveJobs(toRemoveIds);
        }
    }
    
    private void batchRemoveJobs(Set<Long> idsToRemove) {
        if (idsToRemove.isEmpty()) {
            return;
        }
        
        int writeIdx = 0;
        for (int readIdx = 0; readIdx < heap.size(); readIdx++) {
            Job job = heap.get(readIdx);
            if (!idsToRemove.contains(job.getJobId())) {
                if (writeIdx != readIdx) {
                    heap.set(writeIdx, job);
                    indexMap.put(job.getJobId(), writeIdx);
                }
                writeIdx++;
            } else {
                indexMap.remove(job.getJobId());
            }
        }
        
        while (heap.size() > writeIdx) {
            heap.remove(heap.size() - 1);
        }
        
        rebuildHeap();
    }
    
    private void rebuildHeap() {
        int n = heap.size();
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            siftDown(i, heap.get(i));
        }
    }
}
