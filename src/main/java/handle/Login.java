package handle;

import entity.LoginData;
import entity.Result;
import entity.dbEntity.User;
import mapper.UserMapper;
import util.JWTUtil;
import util.JsonUtil;
import util.MyBatisUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//处理登录任务(示例)
@WebServlet("/index/api/login")
public class Login extends HttpServlet {
    private final JsonUtil jsonUtil = new JsonUtil();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = Long.parseLong(req.getParameter("userId"));
        
//        System.out.println("[LOGIN] Querying user: userId=" + userId);
        
//        从数据库查询用户信息
        User user = MyBatisUtil.execute(session -> {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            return userMapper.selectById(userId);
        });
        
        if (user == null) {
//            System.err.println("[LOGIN] User not found: userId=" + userId);
            jsonUtil.sendJson(resp, Result.error("用户不存在"));
            return;
        }
        
        String role = user.getUserRole();
//        System.out.println("[LOGIN] User found: userId=" + userId + ", role=" + role);
        
//    登录时签发令牌
        String token = JWTUtil.generateToken(userId, role);
//        System.out.println("[LOGIN] Token generated successfully");
        
//        登录成功（使用局部变量，线程安全）
        LoginData logindata = new LoginData();
        logindata.setToken(token);
        jsonUtil.sendJson(resp, Result.success(logindata));
        
//        System.out.println("[LOGIN] Login response sent for userId: " + userId + " with role: " + role);
    }
}
