package handle;

import Job.Job;
import core.FlowQueue;
import entity.dbEntity.Pay;
import mapper.PayMapper;
import util.JsonUtil;
import util.MyBatisUtil;
import util.RedisUtil;
import util.TimeUtil;

public class PayTask {
    private final SimpleData data = new SimpleData();
    private final JsonUtil jsonUtil = new JsonUtil();
    
    public Boolean payTask(Job job){
//        System.out.println("[PAY] start:" + TimeUtil.getCurrentMillis() + "JOB:" + job.getJobId());
        job.setStatus(Job.JobStatus.PROCESSING);
        int a = 0;
        data.setA(a);
        
        String dataJsonStr = jsonUtil.objectToJson(data);
        
        long currentTime = TimeUtil.getCurrentMillis();
        long remainingTime = job.getDeadline() - currentTime;
        int ttlSeconds = (int)(remainingTime / 1000);
        
        if(ttlSeconds > 0 && ttlSeconds < 86400 * 30) {
            RedisUtil.setex(String.valueOf(job.getJobId()), ttlSeconds, dataJsonStr);
        } else if (ttlSeconds >= 86400 * 30) {
            RedisUtil.setex(String.valueOf(job.getJobId()), 86400 * 30, dataJsonStr);
        } else {
            RedisUtil.set(String.valueOf(job.getJobId()), dataJsonStr);
        }
        
        try {
            MyBatisUtil.execute(session -> {
                PayMapper payMapper = session.getMapper(PayMapper.class);
                Pay pay = new Pay();
                pay.setMoney(100);
                payMapper.insert(pay);
                return null;
            });
            job.setStatus(Job.JobStatus.SUCCESS);
            return true;
        } catch (Exception e) {
            try {
                MyBatisUtil.execute(session -> {
                    PayMapper payMapper = session.getMapper(PayMapper.class);
                    Pay pay = new Pay();
                    pay.setMoney(0);
                    payMapper.insert(pay);
                    return null;
                });
            } catch (Exception ex) {
                e.printStackTrace();
            }
            
            job.setStatus(Job.JobStatus.FAILED);
            RedisUtil.setex(String.valueOf(job.getJobId()), 60, "FAILED");
//            System.out.println("[PAY] end:" + TimeUtil.getCurrentMillis() + "JOB:" + job.getJobId());
            return false;
        }
    }
}
