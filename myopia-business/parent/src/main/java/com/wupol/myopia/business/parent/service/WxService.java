package com.wupol.myopia.business.parent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.EncryptUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.framework.domain.Result;
import com.wupol.framework.sms.domain.dto.CheckVerifyCodeData;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.parent.client.WxClient;
import com.wupol.myopia.business.parent.constant.WxConstant;
import com.wupol.myopia.business.parent.domain.dto.WxAccessTokenInfo;
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
    @Resource
    private VistelToolsService vistelToolsService;
    @Autowired
    private RedisUtil redisUtil;

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
     * 获取微信access_token
     */
    public WxAccessTokenInfo getAccessTokenWithOpenId(String code) {
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
        return new WxAccessTokenInfo().setOpenId(openId).setAccessToken(accessToken);
    }

    public WxUserInfo getWxUserInfo(WxAccessTokenInfo accessTokenWithOpenId) throws JsonProcessingException {
        String data = wxClient.getUserInfo(accessTokenWithOpenId.getAccessToken(), accessTokenWithOpenId.getOpenId(), "zh_CN");
        logger.debug("获取wx access token返回值: {}", data);
        ObjectMapper objectMapper = new ObjectMapper();
        WxUserInfo wxUserInfo = objectMapper.readValue(data, WxUserInfo.class);
        if (Objects.isNull(wxUserInfo) || StringUtils.isEmpty(wxUserInfo.getNickname())) {
            logger.error("获取微信用户信息，返回值: {}", data);
            throw new BusinessException("【获取微信用户信息异常】：" + JSON.parseObject(data).getString(WxConstant.WX_ERROR_MSG));
        }
        return wxUserInfo;
    }

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

    public void bindPhoneToParent(WxLoginInfo wxLoginInfo) throws IOException {
        // 绑定手机号码到家长用户，同时更新账号与密码
        Parent parent = parentService.findOne(new Parent().setHashKey(wxLoginInfo.getOpenId()));
        oauthService.modifyUser(new UserDTO().setId(parent.getUserId()).setPhone(wxLoginInfo.getPhone()).setUsername(wxLoginInfo.getPhone()).setPassword(parent.getHashKey()));
    }

    /**
     * 发送验证码
     *
     * @param phone
     */
    public Result sendSms(String phone) {
        if (getToken(phone) != null) {
            return Result.Error(String.format("%d分钟内只能发送一次", WxConstant.EXPIRED_MINUTE));
        }
        SmsResult smsResult = sendSmsResult(new MsgData(phone, WxConstant.ZONE));
        String result = "发送成功";
        if (smsResult.isSuccessful()) {
            saveToken(phone, smsResult.getToken());
        } else {
            result = smsResult.getData() != null ? smsResult.getErrorMsg() : smsResult.getMessage();
            logger.error("发送验证码消息到{}失败，原因:{}", phone, result);
        }
        logger.info("发送验证码消息到{}，产生的Token:{}, 完整结果是:{}", phone, smsResult.getToken(), smsResult);
        return smsResult.isSuccessful() ? Result.Ok(result) : Result.Error(result);
    }

    /**
     * 发送验证码
     *
     * @param data
     * @return
     */
    public SmsResult sendSmsResult(MsgData data) {
        try {
            return vistelToolsService.sendVerifyCode(data, "");
        } catch (Exception e) {
            logger.error("发送短信验证码请求失败: " + data.getPhone(), e);
        }
        return new SmsResult(-1, "发送短信验证码失败");
    }

    public Boolean checkVerifyCode(String phone, String verifyCode) {
        String token = getToken(phone);
        // 缓存中无token,即未发送过验证码,直接返回校验失败
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(verifyCode)) {
            return false;
        }
        SmsResult smsResult = checkVerifyCode(new CheckVerifyCodeData(token, verifyCode));
        logger.info("校验Token：{}" + token + "，结果是：" + smsResult.toString());
        Boolean successful = smsResult.isSuccessful();
        if (!successful) {
            preventVulnerability(phone);
        }
        return successful;
    }

    private void preventVulnerability(String phone) {
        String failCountKey = String.format(WxConstant.SMS_TOKEN_FAIL_COUNT, phone);
        long count = redisUtil.incr(failCountKey, 1);
        if (count >= WxConstant.FAIL_MAX_TIME) {
            logger.error("验证码输错过多, 手机号{}的验证码废除", phone);
            removeToken(phone);
            redisUtil.del(failCountKey);
        }
    }

    /**
     * 校验短信验证码请求
     *
     * @param data
     * @return
     */
    public SmsResult checkVerifyCode(CheckVerifyCodeData data) {
        try {
            return vistelToolsService.checkVerifyCode(data);
        } catch (Exception e) {
            logger.error("校验短信验证码请求失败，code: " + data.getCode() + "，token: " + data.getToken(), e);
        }
        return new SmsResult(-1, "校验短信验证码失败");
    }

    private void saveToken(String phone, String token) {
        redisUtil.set(String.format(WxConstant.SMS_REG_PREFIX, phone), token, WxConstant.EXPIRED_SECONDS);
    }

    private String getToken(String phone) {
        Object cache = redisUtil.get(String.format(WxConstant.SMS_REG_PREFIX, phone));
        return cache == null ? null : cache.toString();
    }

    private void removeToken(String phone) {
        redisUtil.del(String.format(WxConstant.SMS_REG_PREFIX, phone));
    }

}
