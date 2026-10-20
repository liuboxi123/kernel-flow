package entity;
//要求前端开始轮询的类
public class JobResult {
    private String jobId; // 仅返回工号（字符串格式，避免JS精度丢失）

    public JobResult() {
    }

    public JobResult(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

}
