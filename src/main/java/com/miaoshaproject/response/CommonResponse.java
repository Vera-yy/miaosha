package com.miaoshaproject.response;

public class CommonResponse {
    //表明对应请求的返回处理结果“success”或“fail”
    private String status;

    //若status=success，则data内返回前端需要的json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;

    //定义一个通用的创建方法
    public static CommonResponse create(Object result){
        return CommonResponse.create(result, "success");
    }

    public static CommonResponse create(Object result, String success){
        CommonResponse response = new CommonResponse();
        response.setStatus(success);
        response.setData(result);
        return  response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
