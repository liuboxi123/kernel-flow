package entity;
//登录成功后返回的json类
public class LoginData {
    private String token;   // 仅返回令牌
    
    public LoginData() {
    }

    public LoginData(String token) {
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}
