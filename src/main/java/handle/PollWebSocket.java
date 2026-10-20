package handle;

import util.WebSocketUtil;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 任务结果推送 WebSocket 端点
 * 仅用于 Poll 接口推送任务执行结果
 */
@ServerEndpoint("/index/ws/poll")
public class PollWebSocket {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    static {
        System.out.println("[WS-ENDPOINT] PollWebSocket class loaded");
    }

    public PollWebSocket() {
        System.out.println("[WS-ENDPOINT] PollWebSocket instance created");
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[WS-ENDPOINT] Client connected: " + session.getId());
        System.out.println("[WS-ENDPOINT] Request URI: " + session.getRequestURI());
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("[WS-ENDPOINT] Client disconnected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            System.out.println("[WS-ENDPOINT] Received message: " + message);
            Map<String, String> msg = OBJECT_MAPPER.readValue(message, Map.class);
            String jobId = msg.get("jobId");

            if (jobId != null && !jobId.isEmpty()) {
                WebSocketUtil.registerSession(jobId, session);
                System.out.println("[WS-ENDPOINT] Registered jobId: " + jobId);
                
                // 发送确认消息
                session.getBasicRemote().sendText("{\"code\":200,\"msg\":\"WebSocket连接已建立\"}");
            }
        } catch (Exception e) {
            System.err.println("[WS-ENDPOINT] Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("[WS-ENDPOINT] Error: " + error.getMessage());
        error.printStackTrace();
    }
}

