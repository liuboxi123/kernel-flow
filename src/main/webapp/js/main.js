// 发送请求类型

document.addEventListener('DOMContentLoaded', function() {
    const rootReq = document.getElementById('root-task');
    const simpleReq = document.getElementById('simple-task');
    const adminReq = document.getElementById('admin-task');
    const loginReq = document.getElementById('login-task');
    const binaryReq = document.getElementById('binary-task');
    const payReq = document.getElementById('pay-task');
    let token = '';
    
    if (loginReq) {
        loginReq.addEventListener("click", function() {
            async function userLogin() {
                try {
                    const data = await get('/api/login', { userId: 1 });
                    console.log('登录成功,token为：', data);
                    token = data.data.token;
                    // alert('登录成功！Token: ' + token.substring(0, 20) + '...');
                } catch (error) {
                    console.error('登录失败:', error.message);
                    // alert('登录失败: ' + error.message);
                }
            }
            userLogin();
        });
    } else {
        console.error('登录按钮未找到！');
    }

    if(rootReq){
        rootReq.addEventListener("click", function() {
            async function rootTask() {
                try {
                    const newUser = { actionType: 'ROOT', token: token };
                    const headers = { 'Authorization': 'Bearer ' + token };

                    console.log('正在提交ROOT任务...');
                    const data = await post('/api/root', newUser, headers);
                    console.log('发送ROOT任务请求成功:', data);
                    
                    if(data.code === 200){
                        const jobId = data.data.jobId;
                        console.log('✅ 任务已提交，jobId:', jobId);
                        console.log('🔌 开始通过 WebSocket 监听任务结果...');
                        
                        // 使用 WebSocket 监听任务结果
                        listenTaskResult(
                            jobId,
                            // 成功回调
                            function(result) {
                                console.log('✅ 任务完成！', result);
                                const taskData = result.data ? result.data.data : null;
                                // alert('任务完成！\nJobId: ' + jobId + '\n结果: ' + JSON.stringify(taskData));
                            },
                            // 失败回调
                            function(result) {
                                console.error('❌ 任务失败！', result);
                                // alert('任务失败！\nJobId: ' + jobId + '\n原因: ' + result.msg);
                            }
                        );
                    } else {
                        console.error('任务提交失败:', data.msg);
                        // alert('任务提交失败: ' + data.msg);
                    }
                } catch (error) {
                    console.error('发送ROOT任务请求失败:', error.message);
                    // alert('请求失败: ' + error.message);
                }
            }
            
            // 提交10个任务测试
            for(var i = 0; i < 10; i++){
                setTimeout(() => rootTask(), i * 100); // 每个任务间隔100ms
            }
        });
    } else {
        console.error('根权限按钮未找到！');
    }
});

