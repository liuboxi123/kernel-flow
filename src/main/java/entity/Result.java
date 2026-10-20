package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import util.ResultCode;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 状态码（建议使用 long 类型，预留更大的编码空间，避免溢出）
    private Long code;
    // 提示信息
    private String msg;
    // 业务数据（使用泛型）
    private T data;

    // 私有构造器，强制通过静态方法创建实例
    private Result() {}

    private Result(Long code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), "操作成功", data);
    }

    /**
     * 失败响应（自定义提示信息）
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 失败响应（自定义状态码和提示信息）
     */
    public static <T> Result<T> error(Long code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 自定义返回事件
     */
    public static <T> Result<T> build(Long code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
