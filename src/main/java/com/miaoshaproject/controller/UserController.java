package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.vo.UserVO;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonResponse;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.Random;

@RestController("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")//跨域问题解决,No 'Access-Control-Allow-Origin' header
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    /**
     * 用户获取otp短信接口
     * @param telphone
     * @return
     */
    @RequestMapping(value = "/getOtp", method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonResponse getOtp(@RequestParam(name = "telphone")String telphone){
        //需要按照一定规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);//随机数取值[0,99999)
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联，用redis（天然适合），暂时用httpsession方式绑定两者
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        //讲OTP验证码通过短信通道发送给用户，暂时省略。实际日志打印用log4j，用户敏感信息也不应日志打印。
        System.out.println("tel="+telphone+" &otpCode="+otpCode);

        return CommonResponse.create(null);
    }


    /**
     * 用户注册
     * @param telphone
     * @param otpCode
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST,consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonResponse register(@RequestParam("otpCode") String otpCode,
                                   @RequestParam("user") String user, @RequestParam("gender")Boolean gender,
                                   @RequestParam("age")Integer age, @RequestParam("telphone")String telphone,
                                   @RequestParam("password")String password) throws Exception{
        //验证手机号和对应的otp是否符合
        String inSessionOptCode = (String) httpServletRequest.getSession().getAttribute(telphone);
        if (!StringUtils.equals(inSessionOptCode, otpCode)){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码验证错误");
        }
        UserModel userModel = new UserModel();
        userModel.setUser(user);
        userModel.setGender(gender);
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        //有问题，MD5Encoder.encode(),只支持16位的md5加密
        //userModel.setEncrptPassword(MD5Encoder.encode(password.getBytes()));
        userModel.setEncrptPassword(MD5(password));

        userService.userRegister(userModel);
        return CommonResponse.create(null);
    }

    private String MD5(String password) throws Exception{
        //确定计算方法
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String passwordSecret = base64Encoder.encode(messageDigest.digest(password.getBytes("utf-8")));
        return passwordSecret;
    }

    /**
     * 用户登录
     * @param telphone
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonResponse login(@RequestParam(name="telphone")String telphone,
                                @RequestParam(name="password")String password) throws Exception{
        //入参校验
        if (StringUtils.isEmpty(telphone)
        ||StringUtils.isEmpty(password)){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号或密码为空");
        }
        UserModel userModel = userService.validateUser(telphone, MD5(password));

        //将登录凭证加入到用户登陆成功的session内（一般登录凭证类似token的，而不是用session，后面分布式用分布式session改造）
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonResponse.create(null);
    }

    @RequestMapping("/getUser")
    @ResponseBody
    public CommonResponse getUser(@RequestParam(name = "id") Integer id) throws Exception{
        UserModel userModel = userService.getUserById(id);

        //若用户信息不存在
        if (userModel == null){
            throw new NullPointerException();
            //throw new BussinessException(EmBusinessError.USER_NOT_EXIST);
        }
        return  CommonResponse.create(convertFromModel(userModel));
    }

    private UserVO convertFromModel(UserModel userModel){
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
