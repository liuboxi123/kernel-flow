package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public ObjectMapper getObjectMapper(){
        return objectMapper;
    }
    public String jsonToString(HttpServletRequest req){
        try{
            req.setCharacterEncoding("utf-8");
            try(BufferedReader reader = req.getReader()){
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public Object StringToObject(String s){
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(s, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object reqJsonToObject(HttpServletRequest req){
        try{
            String json = jsonToString(req);
            return jsonToObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public Object jsonToObject(String json){
        try{
            return objectMapper.readValue(json, Object.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public Boolean sendJson(HttpServletResponse resp, Object o) throws IOException {
        try {
            resp.setContentType("application/json;charset=utf-8");
            String json = objectMapper.writeValueAsString(o);
            resp.getWriter().write(json);
            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

//    将对象转换为jsonStr
    public String objectToJson(Object o){
        try{
            return objectMapper.writeValueAsString(o);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

//    手动挡将序列化完成的json写回resp
    public Boolean sendJsonStr(HttpServletResponse resp, String json){
        try {
            resp.getWriter().write(json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static TaskMeta parseMeta(String rawJson) throws Exception {
        // readTree 只是建立索引视图，不会大量分配业务内存
        JsonNode root = objectMapper.readTree(rawJson);

        TaskMeta meta = new TaskMeta();
        meta.setToken(root.path("token").asText());    // 用户令牌
        meta.setType(root.path("actionType").asText());  // 任务类型

        return meta;
    }
    public static class TaskMeta {
        private String type;    // 存放任务类型
        private String token;    // 存放jwt，若为空则未登录，role字段判断权限
//        getter
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        TaskMeta(){
        }
        TaskMeta(String type){
            this.type = type;
        }
    }
    public String getRawBody(HttpServletRequest req) throws Exception {
        return req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
