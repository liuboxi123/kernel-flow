package priority;

import Job.Job;
import util.TimeUtil;

public class DynamicPriority {

    private static final int MIN_PRIORITY = 1;
    private static final int MAX_PRIORITY = 10;
    private static final long AGING_INTERVAL_MS = 100;
    private static final long URGENCY_THRESHOLD_MS = 5000;
    private static final int PROTECTED_PRIORITY_LEVEL = 3;
    
    public void aging(Job job){
        int staticPriority = job.getStaticPriority();   // 静态优先级
        long submitTime = job.getSubmitTime();  // 任务提交时间
        long deadline = job.getDeadline();  // 任务存活时间
        long currentTime = TimeUtil.getCurrentMillis(); // 当前时间
        long endTime = submitTime + deadline;   // 任务结束时间
        long waitTime = currentTime - submitTime;   // 等待时间
        long remainingTime = endTime - currentTime; // 剩余时间

        if (waitTime <= 0) {    // 超时任务忽略
            return;
        }

//        管理员级别的任务不改动其优先级（普通管理员的任务不会因为时间而跑到超级管理员前面）
        if (staticPriority <= PROTECTED_PRIORITY_LEVEL) {
            return;
        }
        
        int timeScore = calculateTimeScore(waitTime, remainingTime);
        int retryScore = job.getRetryCount();
        
        int newDynamicPriority = staticPriority - (timeScore + retryScore);
        
        newDynamicPriority = Math.max(MIN_PRIORITY, Math.min(MAX_PRIORITY, newDynamicPriority));
        
        if (newDynamicPriority != job.getDynamicPriority()) {
            job.setDynamicPriority(newDynamicPriority);
        }
    }
    
    private int calculateTimeScore(long waitTime, long remainingTime) {
        int score = 0;
        
        if (remainingTime <= 0) {
            score = 9;
        } else if (remainingTime < URGENCY_THRESHOLD_MS) {
            score = 5 + (int)((URGENCY_THRESHOLD_MS - remainingTime) / 1000);
        } else {
            score = (int)(waitTime / AGING_INTERVAL_MS);
        }
        
        return Math.min(score, 9);
    }
}
