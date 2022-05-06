package com.wupol.myopia.oauth.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.oauth.domain.model.VerifyImage;
import com.wupol.myopia.oauth.domain.vo.CaptchaImageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * 图片处理
 *
 * @author hang.yuan 2022/4/29 11:41
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class ImageService {

    private final RedisUtil redisUtil;
    private final VerifyImageService verifyImageService;
    private static final String IMAGE_KEY = "login:image:%s";
    private static final String VERIFY_KEY = "login:verify:%s";
    public static final Integer CAPTCHA_EXPIRATION = 120;
    public static final String SUFFIX = "_myopia";

    @Value("${myopia.login.captchaOff:true}")
    private Boolean captchaOff;

    /**
     * 获取图片
     * @param imageId 图片ID
     */
    public CaptchaImageVO getVerifyImage(Integer imageId){
        if (Objects.isNull(imageId) || imageId < 0 || imageId > 992){
            imageId = new Random().nextInt(992);
        }
        CaptchaImageVO captchaImageVO = new CaptchaImageVO();
        String image = image(imageId);
        captchaImageVO.setImg(image);
        captchaImageVO.setVerify(getVerify(imageId));
        captchaImageVO.setCaptchaOff(captchaOff);
        return captchaImageVO;
    }

    public String getVerify(Integer imageId){
        String verify = IdUtil.fastSimpleUUID()+StrUtil.UNDERLINE+imageId;
        redisUtil.set(String.format(VERIFY_KEY,imageId),verify,CAPTCHA_EXPIRATION);
        return verify;
    }


    public Boolean verify(String verify){
        if (Objects.equals(captchaOff,Boolean.TRUE)){
            if (StrUtil.isNotBlank(verify)){
                String[] split = verify.split(StrUtil.UNDERLINE);
                Integer imageId = null;
                try {
                    imageId = Integer.valueOf(split[1]);
                }catch (Exception e){
                    log.error("图片滑块验证码错误,verify={}",verify);
                    return Boolean.FALSE;
                }
                Object obj = redisUtil.get(String.format(VERIFY_KEY, imageId));
                if (Objects.isNull(obj)){
                    log.info("图片滑块验证过期,verify={}",verify);
                    return Boolean.FALSE;
                }
                boolean equals = Objects.equals(verify, obj.toString());
                if (!equals){
                    log.info("图片滑块验证失败,verify={},redis={}",verify,obj.toString());
                }
                return equals;
            }else {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 根据ID获取图片
     * @param imageId 图片ID
     */
    public String image(Integer imageId) {
        String defaultValue=StrUtil.EMPTY+SUFFIX;
        String key = String.format(IMAGE_KEY, imageId);
        if (redisUtil.hasKey(key)) {
            Object obj = redisUtil.get(key);
            return Optional.ofNullable(obj).map(Object::toString).orElse(defaultValue);
        }else {
            VerifyImage verifyImage = verifyImageService.findById(imageId);
            if (Objects.nonNull(verifyImage)){
                String img = verifyImage.getContent() + SUFFIX;
                redisUtil.set(key, img);
                return img;
            }
        }
        return defaultValue;
    }

}
