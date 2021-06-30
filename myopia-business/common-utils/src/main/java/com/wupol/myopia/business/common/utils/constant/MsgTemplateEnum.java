package com.wupol.myopia.business.common.utils.constant;

import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname MsgTemplateEnum
 * @Description 短信模板管理的枚举类
 * @Date 2021/6/8 3:04 下午
 * @Author Jacob
 * @Version
 */
@AllArgsConstructor
@Getter
public enum MsgTemplateEnum{

    /**
     * 筛查视力的异常提醒
     */
    TO_PARENTS_WARING_KIDS_VISION(1001,"【青少年近视防控】%s同学，你的裸眼视力下降，视功能可能异常。建议：请到医疗机构接受检查，明确诊断并及时采取措施。 ");



    /**
     * 短信编码
     */
    private Integer msgCode;

    /**
     * 短信模板
     */
    private String template;
    /**
     *  key = msgCode, value = template
     */
    private static final Map<Integer, String> MSG_TEMPLATE_MAP;

    static {
        MSG_TEMPLATE_MAP = Arrays.stream(MsgTemplateEnum.values()).collect(Collectors.toMap(MsgTemplateEnum::getMsgCode, MsgTemplateEnum::getTemplate));
    }

    /**
     * 根据msgCode查找template
     * @param msgCode
     * @return
     */
    public static String getTemplateByCode(int msgCode) {
        String template = MSG_TEMPLATE_MAP.get(msgCode);
        if (template == null) {
            throw  new ManagementUncheckedException("无法根据msgCode=" + msgCode + "查找到template.");
        }
        return template;
    }

}
