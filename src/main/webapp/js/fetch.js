// 建议将基础 URL 放在环境变量中，这里仅作示例
const BASE_URL = 'http://localhost:8080/KernelFlow/index';
const WS_URL = 'ws://localhost:8080/KernelFlow/index/ws/poll';

/**
 * 使用 WebSocket 监听任务结果
 * @param {string} jobId - 任务ID
 * @param {function} onSuccess - 成功回调函数，接收 result 对象
 * @param {function} onFailure - 失败回调函数，接收 result 对象
 * @returns {WebSocket} 返回 WebSocket 实例，可手动关闭
 */
function listenTaskResult(jobId, onSuccess, onFailure) {
    console.log('[WS] Connecting to:', WS_URL);
    const ws = new WebSocket(WS_URL);
    
    ws.onopen = function() {
        console.log('[WS] Connected successfully');
        
        // 发送 jobId 注册监听
        const registerMsg = JSON.stringify({ jobId: jobId });
        ws.send(registerMsg);
        console.log('[WS] Registered jobId:', jobId);
    };
    
    ws.onmessage = function(event) {
        console.log('[WS] Received message:', event.data);
        
        try {
            const data = JSON.parse(event.data);
            
            // 任务成功（code === 200）
            if (data.code === 200) {
                console.log('✅ 任务处理完成！', data);
                
                // 调用成功回调
                if (typeof onSuccess === 'function') {
                    onSuccess(data);
                }
                
                // 关闭连接
                ws.close();
            }
            // 任务失败（code === 500）
            else if (data.code === 500) {
                console.error('❌ 任务执行失败:', data.msg);
                
                // 调用失败回调
                if (typeof onFailure === 'function') {
                    onFailure(data);
                }
                
                // 关闭连接
                ws.close();
            }
            // 其他消息（如连接确认）
            else {
                console.log('[WS] Other message:', data);
            }
        } catch (error) {
            console.error('[WS] Failed to parse message:', error);
        }
    };
    
    ws.onerror = function(error) {
        console.error('[WS] Error occurred:', error);
        console.warn('[WS] Falling back to HTTP polling...');
        
        // WebSocket 失败时，降级为轮询
        if (jobId) {
            startSmartPolling(`/api/poll?jobId=${jobId}`, 30);
        }
    };
    
    ws.onclose = function(event) {
        console.log('[WS] Connection closed. Code:', event.code, 'Reason:', event.reason);
    };
    
    return ws;
}

/**
 * 通用智能轮询函数（保留作为 WebSocket 的降级方案）
 * @param {string} url - 轮询的API地址（相对路径，会自动拼接BASE_URL）
 * @param {number} maxRetries - 最大轮询次数
 */
function startSmartPolling(url, maxRetries) {
    let attempt = 0; // 当前轮询次数
    
//    拼接完整的URL
    const fullUrl = `${BASE_URL}${url}`;

    const poll = async () => {
        attempt++;
        console.log(`[POLL] 第 ${attempt} 次轮询请求中...`);

        try {
            const response = await fetch(fullUrl);
            const data = await response.json();

            // 核心判断：code === 200 则结束
            if (data.code === 200) {
                console.log('✅ 任务处理完成，轮询结束！', data);
                return; // 彻底结束
            }
            
//            任务失败（500），停止轮询
            if (data.code === 500) {
                console.error('❌ 任务执行失败:', data.msg);
                return; // 彻底结束
            }
            
//            任务还在处理中（202），继续轮询
            if (data.code === 202) {
                console.log(`⏳ ${data.msg}`);
            }

            // 未达到结束条件，检查是否超出最大次数
            if (attempt >= maxRetries) {
                console.warn('⚠️ 已达到最大轮询次数，任务可能仍在处理中。');
                return;
            }

            // 计算下一次轮询间隔（指数退避：间隔随次数变大，最大不超过30秒）
            // 规律大致为：1s -> 2s -> 4s -> 8s -> 16s -> 30s...
            const baseDelay = Math.min(1000 * Math.pow(2, attempt - 1), 30000);
            const jitter = Math.random() * 500; // 0-500ms 随机延迟
            const delay = baseDelay + jitter;
            console.log(`[POLL] 任务未完成，${delay}ms 后进行下一次轮询...`);

            setTimeout(poll, delay); // 递归调用

        } catch (error) {
            console.error('[POLL] 轮询请求发生异常:', error);
            // 即使报错，只要没超过次数，依然继续尝试
            if (attempt < maxRetries) {
                setTimeout(poll, 2000); // 报错后固定2秒后重试
            }
        }
    };

    // 立即开始第一次轮询
    poll();
}

/**
 * 通用 Fetch 请求封装
 * @param {string} url - 请求路径 (例如: '/users')
 * @param {object} options - 请求配置项
 * @param {string} options.method - 请求方法 (默认 'GET')
 * @param {object} options.params - GET 请求的查询参数 (例如: { id: 1 })
 * @param {object} options.data - POST/PUT 等请求的 body 数据
 * @param {object} options.headers - 自定义请求头
 * @returns {Promise<any>} - 返回解析后的 JSON 数据
 */
async function request(url, options = {}) {
    const { method = 'GET', params, data, headers = {} } = options;

    // 1. 处理请求头，默认添加 JSON 类型
    const defaultHeaders = {
        'Content-Type': 'application/json',
        ...headers,
    };

    // 2. 处理 GET 请求的查询参数 (例如: /users?id=1&name=abc)
    let fetchUrl = `${BASE_URL}${url}`;
    if (params && method.toUpperCase() === 'GET') {
        const queryString = new URLSearchParams(params).toString();
        fetchUrl += `?${queryString}`;
    }

    // 3. 配置 fetch 的选项
    const fetchOptions = {
        method,
        headers: defaultHeaders,
    };

    // 4. 处理非 GET 请求的 body 数据，将对象序列化为 JSON 字符串
    if (data && ['POST', 'PUT', 'PATCH'].includes(method.toUpperCase())) {
        fetchOptions.body = JSON.stringify(data);
    }

    try {
        // 5. 发起请求
        const response = await fetch(fetchUrl, fetchOptions);

        // 6. 统一处理 HTTP 状态码错误 (fetch 只有在网络故障时才会 reject，404/500 需要手动判断)
        if (!response.ok) {
            throw new Error(`请求失败，状态码: ${response.status}`);
        }

        // 7. 解析并返回 JSON 数据
        const result = await response.json();
        return result;
    } catch (error) {
        console.error('Fetch 请求发生错误:', error);
        throw error;
    }
}

function get(url, params = {}, headers = {}) {
    return request(url, { method: 'GET', params, headers });
}

function post(url, data = {}, headers = {}) {
    return request(url, { method: 'POST', data, headers });
}
