package com.wupol.myopia.business.common.utils.util;

import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * @Classname MsgContentUtil
 * @Description 用于将模板和参数合成在一起
 * @Date 2021/6/8 3:53 下午
 * @Author Jacob
 * @Version
 */
@UtilityClass
public class MsgContentUtil {

    /**
     * 获取参数内容
     * @param msgTemplateEnum
     * @param params 请在入参前检查参数的数量,和模板中的%s是否是一致的,如果参数数量低于template里的%s的数量,会报错MissingFormatArgumentException
     * @return
     */
    public String getMsgContent(MsgTemplateEnum msgTemplateEnum,Object ...params) {
        String template = msgTemplateEnum.getTemplate();
        return String.format(template, params);
    }

    /**
     * 获取参数内容
     * @param msgTemplateEnum
     * @param params 请在入参前检查参数的数量,和模板中的%s是否是一致的,如果参数数量低于template里的%s的数量,会报错MissingFormatArgumentException
     * @return
     */
    public String getMsgContent(MsgTemplateEnum msgTemplateEnum, Collection<Object> params) {
        String template = msgTemplateEnum.getTemplate();
        return String.format(template, params);
    }

}
