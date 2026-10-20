package util;

public enum ResultCode {
    SUCCESS(200L, "操作成功"),
    FAIL(500L, "服务器错误"),
    POLL(202L, "操作成功，请稍后查询结果"),
    PARAM_ERROR(400L, "参数错误"),
    UNAUTHORIZED(401L, "未授权"),
    NOT_FOUND(404L, "资源不存在");

    private final Long code;
    private final String msg;

    ResultCode(Long code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Long getCode() { return code; }
    public String getMsg() { return msg; }
}
