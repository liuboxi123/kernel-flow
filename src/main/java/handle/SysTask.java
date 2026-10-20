package handle;

import Job.Job;
import core.FlowQueue;
import util.TimeUtil;

public class SysTask {
    public static void sysTask(Job job){
        Long endTime = job.getSubmitTime() + job.getDeadline();
        Long now = TimeUtil.getCurrentMillis();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            int retryCount = job.getRetryCount();
            retryCount++;
            if(retryCount <= job.getMaxRetries() || endTime < now){
                FlowQueue.getPartition(job.getJobId()).offer(job);
//                System.out.println("[SYS] System task retry " + retryCount + " times");
            }
        }
    }
}
