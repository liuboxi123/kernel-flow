package Job;

import util.IdUtil;
import util.TimeUtil;

public class InitJob {
    private static final ThreadLocal<IdUtil> LOCAL_ID_GENERATOR = ThreadLocal.withInitial(() -> new IdUtil(1, 1));    public Job jobInit(String payload,
                    int staticPriority,
                    String actionType,
                    int maxRetries,
                    long deadline,
                    String userRole){
        int d = staticPriority;
        Job job = new Job();
        long currentSysTime = System.currentTimeMillis();;
        long jobId = LOCAL_ID_GENERATOR.get().nextId();
        job.setJobId(jobId);
        job.setSubmitTime(currentSysTime);
        job.setStatus(Job.JobStatus.PENDING);
        job.setPayload(payload);
        job.setRetryCount(0);
        job.setDeadline(deadline);
        job.setMaxRetries(maxRetries);
        if(userRole != null && !userRole.isEmpty() && !"PAYMENT".equals(userRole)){
            job.setActionType(userRole);
        } else{
            job.setActionType(actionType);
        }
        job.setStaticPriority(staticPriority);
        job.setDynamicPriority(d);
//        System.out.println("[INITJOB] end: --id" + job.getJobId() + "--time:" + TimeUtil.getCurrentMillis());
        return job;
    }
}
