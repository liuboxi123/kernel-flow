package handle;

import Job.Job;
import core.FlowQueue;
import util.JsonUtil;
import util.RedisUtil;
import util.TimeUtil;

public class RootTask {
    private final SimpleData data = new SimpleData();
    private final JsonUtil jsonUtil = new JsonUtil();
    public Boolean rootTask(Job job){
        job.setStatus(Job.JobStatus.PROCESSING);
        int a = 100;
        if(true){
            data.setA(a);
            String dataJsonStr = jsonUtil.objectToJson(data);
            long remainingTime = job.getDeadline();
            int ttlSeconds = (int)(remainingTime / 1000);

            if(ttlSeconds > 0 && ttlSeconds < 86400 * 30) {
                RedisUtil.setex(String.valueOf(job.getJobId()), ttlSeconds, dataJsonStr);
            } else if (ttlSeconds >= 86400 * 30) {
                RedisUtil.setex(String.valueOf(job.getJobId()), 86400 * 30, dataJsonStr);
            } else {
                RedisUtil.set(String.valueOf(job.getJobId()), dataJsonStr);
            }
            job.setStatus(Job.JobStatus.SUCCESS);
            return true;
        } else {
            job.setStatus(Job.JobStatus.FAILED);
            
            boolean canRetry = job.getRetryCount() < job.getMaxRetries();
            boolean notExpired = job.getDeadline() > TimeUtil.getCurrentMillis();
            
            if(canRetry && notExpired){
                int retry = job.getRetryCount() + 1;
                job.setRetryCount(retry);
                FlowQueue.getPartition(job.getJobId()).offer(job);
                job.setStatus(Job.JobStatus.PROCESSING);
            } else {
                String failedMsg = "FAILED";
                if(!canRetry) {
                    failedMsg = "FAILED:MAX_RETRIES_EXCEEDED";
                }
                if(!notExpired) {
                    failedMsg = "FAILED:TIMEOUT";
                }
                RedisUtil.setex(String.valueOf(job.getJobId()), 60, failedMsg);
            }
            return false;
        }
    }
}
