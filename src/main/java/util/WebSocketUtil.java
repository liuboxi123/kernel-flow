package util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 工具类
 * 用于 Poll 接口推送任务结果
 */
public class WebSocketUtil {
    
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * 注册会话
     */
    public static void registerSession(String jobId, Session session) {
        SESSION_MAP.put(jobId, session);
    }
    
    /**
     * 移除会话
     */
    public static void removeSession(String jobId) {
        SESSION_MAP.remove(jobId);
    }
    
    /**
     * 推送任务结果
     */
    public static void pushResult(String jobId, Object result) {
        Session session = SESSION_MAP.get(jobId);
        if (session != null && session.isOpen()) {
            try {
                String json = OBJECT_MAPPER.writeValueAsString(result);
                session.getBasicRemote().sendText(json);
                
                // 推送后移除
                SESSION_MAP.remove(jobId);
                System.out.println("[WS-UTIL] Pushed result to jobId: " + jobId);
            } catch (IOException e) {
                System.err.println("[WS-UTIL] Failed to push: " + e.getMessage());
            }
        }
    }
}

