package handle;

import entity.PollData;
import entity.Result;
import util.JsonUtil;
import util.RedisUtil;
import util.WebSocketUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/index/api/poll")
public class Poll extends HttpServlet {
    private final JsonUtil jsonUtil = new JsonUtil();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jobId = req.getParameter("jobId");
        
//        检查 jobId 是否为空
        if (jobId == null || jobId.isEmpty()) {
            jsonUtil.sendJson(resp, Result.error("缺少 jobId 参数"));
            return;
        }
        
        String date = RedisUtil.get(jobId);
        
        if(date != null && !date.startsWith("FAILED")){
//            直接使用 Redis 中的原始 JSON 字符串作为 data 字段
            PollData<String> pollData = new PollData<>();
            pollData.setData(date);  // 直接存储原始 JSON 字符串
            pollData.setJobId(jobId);
            
            Result result = Result.success(pollData);
            
//            通过 WebSocket 推送
            WebSocketUtil.pushResult(jobId, result);
            
//            同时返回 HTTP 响应（兼容）
            jsonUtil.sendJson(resp, result);
            
            RedisUtil.del(jobId);
        } else if (date != null && date.startsWith("FAILED")) {
            String failReason = "任务执行失败";
            if("FAILED:MAX_RETRIES_EXCEEDED".equals(date)) {
                failReason = "任务失败：重试次数用尽";
            } else if("FAILED:TIMEOUT".equals(date)) {
                failReason = "任务失败：已超时";
            }
            
            PollData pollData = new PollData();
            pollData.setJobId(jobId);
            pollData.setData(null);
            
            Result result = Result.build(500L, failReason, pollData);
            
//            通过 WebSocket 推送失败
            WebSocketUtil.pushResult(jobId, result);
            
            jsonUtil.sendJson(resp, result);
            RedisUtil.del(jobId);
        } else {
            PollData pollData = new PollData();
            pollData.setJobId(jobId);
            pollData.setData(null);
            jsonUtil.sendJson(resp, Result.build(202L, "任务处理中", pollData));
        }
    }
}
