package Job;

import java.util.Objects;

public class Job implements Comparable<Job> {
    public Job(){
    }
    public Job(long jobId, int staticPriority, long submitTime, long deadline, int maxRetries, String payload, String actionType, int retryCount, JobStatus status){
        this.jobId = jobId;
        this.staticPriority = staticPriority;
        this.submitTime = submitTime;
        this.deadline = deadline;
        this.maxRetries = maxRetries;
        this.payload = payload;
        this.actionType = actionType;
        this.retryCount = retryCount;
        this.status = status;
    }
    private long jobId;
    private int staticPriority; // 静态优先级（任务初始时获取）
    private int dynamicPriority;    // 动态优先级（由老化算法控制）
    private long submitTime;
    private long deadline;
    private int maxRetries;
    private String payload;
    private String actionType;
    private int retryCount;
    private JobStatus status;
    
    public enum JobStatus{
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        DEAD
    }
    
    @Override
    public int compareTo(Job other) {
        return Integer.compare(this.getEffectivePriority(), other.getEffectivePriority());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return jobId == job.jobId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(jobId);
    }
    
    @Override
    public String toString() {
        return "Job{" +
                "jobId=" + jobId +
                ", staticPriority=" + staticPriority +
                ", submitTime=" + submitTime +
                ", deadline=" + deadline +
                ", maxRetries=" + maxRetries +
                ", payload='" + payload + '\'' +
                ", actionType='" + actionType + '\'' +
                ", retryCount=" + retryCount +
                ", status=" + status +
                '}';
    }
    
    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getStaticPriority() {
        return staticPriority;
    }

    public void setStaticPriority(int staticPriority) {
        this.staticPriority = staticPriority;
        this.dynamicPriority = staticPriority;
    }

    public int getDynamicPriority() {
        return dynamicPriority;
    }

    public void setDynamicPriority(int dynamicPriority) {
        this.dynamicPriority = dynamicPriority;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public int getEffectivePriority() {
        return (this.dynamicPriority + this.staticPriority) / 2;
    }
}
