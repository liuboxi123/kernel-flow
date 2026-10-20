package entity;


//前端轮询反馈的类
public class PollData<T> {
    private T data;    // 序列化好的放在内存里的json字符串
    private String jobId;   // 改为String，避免JS精度丢失
    
    public PollData(){
    }

    public PollData(String jobId,T data){
        this.data = data;
        this.jobId = jobId;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
