package handle;

import Job.Job;
import core.FlowQueue;
import util.JsonUtil;
import util.RedisUtil;
import util.TimeUtil;

import java.util.Random;

public class SimpleTask {
    private final SimpleData data = new SimpleData();
    private final JsonUtil jsonUtil = new JsonUtil();
    public Boolean simpleTask(Job job){
        job.setStatus(Job.JobStatus.PROCESSING);
        try {
            Thread.sleep(30000 + new Random().nextInt(10000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 20000) {
            Math.random();
        }
        int a = 0;
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
            if(job.getRetryCount() < job.getMaxRetries() && job.getDeadline() > TimeUtil.getCurrentMillis()){
                int retry = job.getRetryCount() + 1;
                job.setRetryCount(retry);
                FlowQueue.getPartition(job.getJobId()).offer(job);
                job.setStatus(Job.JobStatus.PENDING);
            } else {
                RedisUtil.setex(String.valueOf(job.getJobId()), 60, "FAILED");
            }
            return false;
        }
    }
}
class SimpleData {
    private int a;
    public SimpleData(int a){
        this.a = a;
    }
    public int getA(){
        return a;
    }
    public void setA(int a){
        this.a = a;
    }
    public SimpleData(){}
}
