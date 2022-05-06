package com.wupol.myopia.oauth.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 验证图片实体
 *
 * @author hang.yuan 2022/5/5 15:51
 */
@Data
@Accessors(chain = true)
public class CaptchaImageVO implements Serializable {
    /**
     * 验证
     */
    private String verify;
    /**
     * 图片
     */
    private String img;
    /**
     * 验证开关
     */
    private Boolean captchaOff;
}
