package core;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Set;

/**
 * WebSocket 配置类
 * 用于注册 WebSocket Endpoint
 */
public class WebSocketConfig implements ServerApplicationConfig {

    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> scanned) {
        // 编程式注册 Endpoint（如果需要）
        return null;
    }

    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        // 返回所有使用 @ServerEndpoint 注解的类
        System.out.println("[WS-CONFIG] Scanning for WebSocket endpoints...");
        System.out.println("[WS-CONFIG] Found " + scanned.size() + " annotated endpoint(s)");

        for (Class<?> clazz : scanned) {
            System.out.println("[WS-CONFIG] - " + clazz.getName());
        }

        // 返回所有扫描到的注解端点
        return scanned;
    }
}

