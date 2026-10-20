import Job.Job;
import Job.InitJob;
import core.FlowQueue;
import entity.JobResult;
import entity.Result;
import priority.PriorityEvaluator;
import router.EvaluatorRouter;
import util.JWTUtil;
import util.JsonUtil;
import util.TimeUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class pipe implements Filter {
    private final JsonUtil jsonUtil = new JsonUtil();
    private final PriorityEvaluator priorityEvaluator = new PriorityEvaluator();
    private final EvaluatorRouter evaluatorRouter = new EvaluatorRouter();
    private final InitJob jobInit = new InitJob();
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        System.out.println("[PIPE]: get req -- time:" + TimeUtil.getCurrentMillis());
        servletResponse.setContentType("text/html;charset=utf-8");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String uri = request.getRequestURI();

        if (isExcludedEndpoint(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jsonStr;
        try {
            jsonStr = jsonUtil.getRawBody(request);
        } catch (Exception e) {
//            e.printStackTrace();
            return;
        }

        Job job = null;
        String type = "";
        String token = "";

        try {
            JsonUtil.TaskMeta meta = JsonUtil.parseMeta(jsonStr);
            type = meta.getType();
            token = meta.getToken();
        }catch (Exception e){

//            e.printStackTrace();
            return;
        }

        String userRole = JWTUtil.getUserRoleFromToken(token);
        String priorityStr = evaluatorRouter.staticPriority(userRole,type);
        long deadline = evaluatorRouter.deadLine(priorityStr);
        int maxRetries = evaluatorRouter.maxRetries(priorityStr);

        job = jobInit.jobInit(
                jsonStr,
                priorityEvaluator.staticPriority(priorityStr),
                type,
                maxRetries,
                deadline,
                userRole
        );

        if (job != null) {
            FlowQueue partition = FlowQueue.getPartition(job.getJobId());
//            System.out.println("[PIPE]:job into queue -- jobId:" + job.getJobId() + "time:" + TimeUtil.getCurrentMillis());
            partition.offer(job);
        }

        Result<JobResult> result;
        JobResult jobIdResp = new JobResult();

        if(job != null){
            jobIdResp.setJobId(String.valueOf(job.getJobId()));
            result = Result.success(jobIdResp);
            String jsonResponse = jsonUtil.objectToJson(result);
            jsonUtil.sendJson((HttpServletResponse)servletResponse,result);
        } else {
            jsonUtil.sendJson((HttpServletResponse)servletResponse,Result.error("任务创建失败"));
        }
    }

    private boolean isExcludedEndpoint(String uri) {
        if (uri.endsWith(".html") || 
            uri.endsWith(".css") || 
            uri.endsWith(".js") ||
            uri.endsWith(".png") ||
            uri.endsWith(".jpg") ||
            uri.endsWith(".jpeg") ||
            uri.endsWith(".gif") ||
            uri.endsWith(".ico")) {
            return true;
        }
        if (uri.contains("/api/login") ||
                uri.contains("/api/poll") ||
                uri.contains("/api/binaryStream")) {
            return true;
        }
        
        if (uri.contains("/ws/")) {
//            System.out.println("[PIPE] WebSocket request detected, passing through: " + uri);
            return true;
        }
        
        return false;
    }
}
