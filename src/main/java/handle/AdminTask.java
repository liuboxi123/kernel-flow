package handle;

import Job.Job;
import core.FlowQueue;
import util.JsonUtil;
import util.RedisUtil;
import util.TimeUtil;

public class AdminTask {
    private final SimpleData data = new SimpleData();
    private final JsonUtil jsonUtil = new JsonUtil();
    public Boolean adminTask(Job job){
        job.setStatus(Job.JobStatus.PROCESSING);
        int a = 0;
        if(true){
            data.setA(a);
            String dataJsonStr = jsonUtil.objectToJson(data);
            long remainingTime = job.getDeadline();
            int ttlSeconds = (int)(remainingTime / 1000);
            if(ttlSeconds > 0) {
                RedisUtil.setex(String.valueOf(job.getJobId()), ttlSeconds, dataJsonStr);
            } else {
                RedisUtil.set(String.valueOf(job.getJobId()), dataJsonStr);
            }
            job.setStatus(Job.JobStatus.SUCCESS);
            return true;
        } else {
//            System.out.println("[ADMIN] JobId:" + job.getJobId() + " Failed");
            job.setStatus(Job.JobStatus.FAILED);
            if(job.getRetryCount() < job.getMaxRetries() && job.getDeadline() > TimeUtil.getCurrentMillis()){
                int retry = job.getRetryCount() + 1;
                job.setRetryCount(retry);
                FlowQueue.getPartition(job.getJobId()).offer(job);
                job.setStatus(Job.JobStatus.PROCESSING);
//                System.out.println("[ADMIN] JobId:" + job.getJobId() + " Retry " + retry);
            } else {
                RedisUtil.setex(String.valueOf(job.getJobId()), 60, "FAILED");
//                System.out.println("[ADMIN] JobId:" + job.getJobId() + " Marked as FAILED in Redis");
            }
            return false;
        }
    }
}
