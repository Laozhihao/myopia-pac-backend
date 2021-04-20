package com.wupol.myopia.business.api.parent.service;

import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.framework.sms.domain.dto.CheckVerifyCodeData;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.parent.constant.SmsConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2021/3/5
 **/
@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Resource
    private VistelToolsService vistelToolsService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 发送短信验证码
     *
     * @param phone 手机号码
     * @return void
     **/
    public void sendSms(String phone) {
        if (getToken(phone) != null) {
            throw new BusinessException(String.format("%d分钟内只能发送一次", SmsConstant.EXPIRED_MINUTE));
        }
        SmsResult smsResult = sendSmsResult(new MsgData(phone, SmsConstant.ZONE));
        if (!smsResult.isSuccessful()) {
            String result = smsResult.getData() != null ? smsResult.getErrorMsg() : smsResult.getMessage();
            logger.error("发送验证码消息到"+ phone +"失败，原因：" + result);
            throw new BusinessException("发送验证码失败");
        }
        saveToken(phone, smsResult.getToken());
        logger.info("发送验证码消息到{}，产生的Token：{}, 完整结果是：{}", phone, smsResult.getToken(), smsResult);
    }

    /**
     * 发送短信验证码
     *
     * @param data
     * @return
     */
    private SmsResult sendSmsResult(MsgData data) {
        try {
            return vistelToolsService.sendVerifyCode(data, StringUtils.EMPTY);
        } catch (Exception e) {
            logger.error("发送短信验证码请求失败: " + data.getPhone(), e);
        }
        return new SmsResult(-1, "发送短信验证码失败");
    }

    /**
     * 校验短信验证码
     *
     * @param phone 手机号码
     * @param smsCode 验证码
     * @return java.lang.Boolean
     **/
    public Boolean checkSmsCode(String phone, String smsCode) {
        Assert.hasLength(phone, "手机号码不能为空");
        Assert.hasLength(smsCode, "验证码不能为空");
        String token = getToken(phone);
        // 缓存中无token，即未发送过验证码，直接返回校验失败
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        SmsResult smsResult = checkSmsCode(new CheckVerifyCodeData(token, smsCode));
        logger.info("校验Token：{}" + token + "，结果是：" + smsResult.toString());
        Boolean successful = smsResult.isSuccessful();
        if (!successful) {
            preventVulnerability(phone);
        }
        return successful;
    }

    /**
     * 废除短信验证码
     *
     * @param phone 手机号码
     * @return void
     **/
    private void preventVulnerability(String phone) {
        String failCountKey = String.format(CacheKey.SMS_TOKEN_FAIL_COUNT, phone);
        long count = redisUtil.incr(failCountKey, 1);
        if (count >= SmsConstant.FAIL_MAX_TIME) {
            logger.error("验证码输错过多, 手机号{}的验证码废除", phone);
            removeToken(phone);
            redisUtil.del(failCountKey);
        }
    }

    /**
     * 校验短信验证码请求
     *
     * @param data 验证码信息
     * @return com.wupol.framework.sms.domain.dto.SmsResult
     **/
    private SmsResult checkSmsCode(CheckVerifyCodeData data) {
        try {
            return vistelToolsService.checkVerifyCode(data);
        } catch (Exception e) {
            logger.error("校验短信验证码请求失败，code: " + data.getCode() + "，token: " + data.getToken(), e);
        }
        return new SmsResult(-1, "校验短信验证码失败");
    }

    /**
     * 保存token
     *
     * @param phone 手机号码
     * @param token token
     * @return void
     **/
    private void saveToken(String phone, String token) {
        redisUtil.set(String.format(CacheKey.SMS_CODE_TOKEN, phone), token, SmsConstant.EXPIRED_SECONDS);
    }

    /**
     * 获取token
     *
     * @param phone 手机号码
     * @return java.lang.String
     **/
    private String getToken(String phone) {
        Object cache = redisUtil.get(String.format(CacheKey.SMS_CODE_TOKEN, phone));
        return cache == null ? null : cache.toString();
    }

    /**
     * 移除token
     *
     * @param phone 手机号码
     * @return void
     **/
    private void removeToken(String phone) {
        redisUtil.del(String.format(CacheKey.SMS_CODE_TOKEN, phone));
    }
}
