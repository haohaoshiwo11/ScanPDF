package cn.com.dyg.dto;

import java.io.Serializable;

public class ResultVO implements Serializable {
    /**
     * 状态:200（成功）；404（失败）
     */
    private String status;
    /**
     * 消息
     */
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
