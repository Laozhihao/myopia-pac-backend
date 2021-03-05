package com.wupol.myopia.business.parent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.EncryptUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.parent.client.WxClient;
import com.wupol.myopia.business.parent.constant.WxConstant;
import com.wupol.myopia.business.parent.domain.dto.WxAuthorizationInfo;
import com.wupol.myopia.business.parent.domain.dto.WxLoginInfo;
import com.wupol.myopia.business.parent.domain.dto.WxUserInfo;
import com.wupol.myopia.business.parent.domain.model.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * 微信授权与登录处理相关
 *
 * @Author HaoHao
 * @Date 2021/2/26
 **/
@Service
public class WxService {
    private static final Logger logger = LoggerFactory.getLogger(WxService.class);

    @Value("${wechat.app.id}")
    private String appId;
    @Value("${wechat.app.secret}")
    private String appSecret;

    @Autowired
    private ParentService parentService;
    @Autowired
    private OauthService oauthService;
    @Resource
    private WxClient wxClient;

    /**
     * 根据微信回调的code获取openId
     *
     * @param code 微信回调的code
     * @return java.lang.String
     **/
    public String getOpenId(String code) {
        Assert.hasLength(code, "【获取OpenId失败】code为空");
        try {
            String data = wxClient.getAccessToken(appId, appSecret, code, "authorization_code");
            logger.debug("获取openId返回值: {}", data);
            return JSON.parseObject(data).getString("openid");
        } catch (Exception e) {
            throw new BusinessException("获取OpenId失败", e);
        }
    }

    /**
     * 获取微信accessToken和openId
     *
     * @param code 微信回调的code
     * @return com.wupol.myopia.business.parent.domain.dto.WxAuthorizationInfo
     **/
    public WxAuthorizationInfo getAccessTokenAndOpenId(String code) {
        Assert.hasLength(code, "微信code不能为空");
        String data = wxClient.getAccessToken(appId, appSecret, code, "authorization_code");
        logger.debug("获取wx access token返回值: {}", data);
        JSONObject dataJson = JSON.parseObject(data);
        String accessToken = dataJson.getString(WxConstant.ACCESS_TOKEN);
        String openId = dataJson.getString("openid");
        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(openId)) {
            logger.error("获取wx access token返回值: {}", dataJson);
            throw new BusinessException(dataJson.getString(WxConstant.WX_ERROR_MSG), dataJson.getInteger(WxConstant.WX_ERROR_CODE));
        }
        return new WxAuthorizationInfo().setOpenId(openId).setAccessToken(accessToken);
    }

    /**
     * 获取微信个人信息
     *
     * @param wxAuthorizationInfo 微信access_token和openId等授权信息
     * @return com.wupol.myopia.business.parent.domain.dto.WxUserInfo
     **/
    public WxUserInfo getWxUserInfo(WxAuthorizationInfo wxAuthorizationInfo) throws JsonProcessingException {
        String data = wxClient.getUserInfo(wxAuthorizationInfo.getAccessToken(), wxAuthorizationInfo.getOpenId(), "zh_CN");
        logger.debug("获取wx access token返回值: {}", data);
        ObjectMapper objectMapper = new ObjectMapper();
        WxUserInfo wxUserInfo = objectMapper.readValue(data, WxUserInfo.class);
        if (Objects.isNull(wxUserInfo) || StringUtils.isEmpty(wxUserInfo.getNickname())) {
            logger.error("获取微信用户信息，返回值: {}", data);
            throw new BusinessException("【获取微信用户信息异常】：" + JSON.parseObject(data).getString(WxConstant.WX_ERROR_MSG));
        }
        return wxUserInfo;
    }

    /**
     * 创建家长
     *
     * @param wxUserInfo 微信用户个人信息
     * @return com.wupol.myopia.business.parent.domain.model.Parent
     **/
    @Transactional(rollbackFor = Exception.class)
    public Parent addParentAndUser(WxUserInfo wxUserInfo) {
        Assert.notNull(wxUserInfo, "微信用户信息不能为空");
        // 创建家长
        Parent parent = new Parent().setOpenId(wxUserInfo.getOpenId()).setHashKey(EncryptUtils.md5Base64(wxUserInfo.getOpenId())).setWxNickname(wxUserInfo.getNickname()).setWxHeaderImgUrl(wxUserInfo.getHeadImgUrl());
        parentService.save(parent);
        // 新增用户
        UserDTO userDTO = new UserDTO().setUsername(wxUserInfo.getOpenId()).setPassword(parent.getHashKey()).setGender(wxUserInfo.getSex()).setOrgId(-1).setSystemCode(SystemCode.PATENT_CLIENT.getCode());
        userDTO = oauthService.addUser(userDTO);
        // 更新家长
        parentService.updateById(new Parent().setId(parent.getId()).setUserId(userDTO.getId()));
        return parent;
    }

    /**
     * 绑定家长手机号码
     *
     * @param wxLoginInfo 手机信息
     * @return void
     **/
    public void bindPhoneToParent(WxLoginInfo wxLoginInfo) throws IOException {
        // 绑定手机号码到家长用户，同时更新账号与密码
        Parent parent = parentService.findOne(new Parent().setHashKey(wxLoginInfo.getOpenId()));
        oauthService.modifyUser(new UserDTO().setId(parent.getUserId()).setPhone(wxLoginInfo.getPhone()).setUsername(wxLoginInfo.getPhone()).setPassword(parent.getHashKey()));
    }
}
