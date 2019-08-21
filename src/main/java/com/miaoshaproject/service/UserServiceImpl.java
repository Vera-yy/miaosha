package com.miaoshaproject.service;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import java.beans.Transient;
import java.security.MessageDigest;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null){
            return null;
        }
        UserPasswordDO userPasswordDO= userPasswordDOMapper.selectByUserId(id);
        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    @Transient
    public void userRegister(UserModel userModel) throws BussinessException{
        if(userModel==null){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        if(StringUtils.isEmpty(userModel.getUser())
        ||userModel.getGender()==null
        ||StringUtils.isEmpty(userModel.getTelphone())
        ||userModel.getAge()==null){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        try {
            userDOMapper.insertSelective(userDO);//keyProperty="id" useGeneratedKeys="true",用于取出自增id
        }catch (DuplicateKeyException e){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已重复");
        }

        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userDO.getId());
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    @Override
    public UserModel validateUser(String telphone, String password) throws BussinessException {
        //根据telphone获取UserDO
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null){
            throw new BussinessException(EmBusinessError.USER_OR_PASSWORD_ERROR);
        }

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        //匹配password
        String passwordMD5 = userPasswordDO.getEncrptPassword();
        if (!StringUtils.equals(password, passwordMD5)){
            throw new BussinessException(EmBusinessError.USER_OR_PASSWORD_ERROR);
        }

        return userModel;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);

        if (userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
