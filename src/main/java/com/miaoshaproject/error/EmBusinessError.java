package com.miaoshaproject.error;

public enum EmBusinessError implements CommonError {
    //通用错误类型10000
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),
    //20000开头是用户信息相关错误定义
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_OR_PASSWORD_ERROR(20002,"用户或密码错误"),
    ;

    private EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    private int errCode;
    private String errMsg;
    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
