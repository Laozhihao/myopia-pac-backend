package com.wupol.myopia.base.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;

/**
 * 密码生成器
 *
 * @Author HaoHao
 * @Date 2020/12/29
 **/
@UtilityClass
public class PasswordGenerator {

    /** 筛查人员密码截取长度 */
    private static final int SCREENING_ADMIN_PWD_SUB_LENGTH = 4;
    /** 密码后缀随机数长度 */
    private static final int PASSWORD_SUFFIX_RANDOM_LENGTH = 11;

    /** 管理端用户密码前缀 */
    private static final String MANAGEMENT_USER_PWD_PREFIX = "g";
    /** 学校端管理员用户密码前缀 */
    private static final String SCHOOL_ADMIN_PWD_PREFIX = "x";
    /** 医院端管理员用户密码前缀 */
    private static final String HOSPITAL_ADMIN_PWD_PREFIX = "y";
    /** 筛查机构管理端的管理员用户密码前缀 */
    private static final String SCREENING_ORG_ADMIN_PWD_PREFIX = "s";

    /**
     * 管理端用户密码
     *
     * @return java.lang.String
     **/
    public static String getManagementUserPwd() {
        // 开头字母j + 11位字母或数字
        return MANAGEMENT_USER_PWD_PREFIX + RandomUtil.randomString(PASSWORD_SUFFIX_RANDOM_LENGTH);
    }

    /**
     * 学校端管理员密码
     *
     * @return java.lang.String
     **/
    public static String getSchoolAdminPwd() {
        // 开头字母x + 11位字母或数字
        return SCHOOL_ADMIN_PWD_PREFIX + RandomUtil.randomString(PASSWORD_SUFFIX_RANDOM_LENGTH);
    }

    /**
     * 医院端管理员用户密码
     *
     * @return java.lang.String
     **/
    public static String getHospitalAdminPwd() {
        // 开头字母y + 11位字母或数字
        return HOSPITAL_ADMIN_PWD_PREFIX + RandomUtil.randomString(PASSWORD_SUFFIX_RANDOM_LENGTH);
    }

    /**
     * 筛查机构管理端的管理员用户密码
     *
     * @return java.lang.String
     **/
    public static String getScreeningAdminPwd() {
        // 开头字母y + 11位字母或数字
        return SCREENING_ORG_ADMIN_PWD_PREFIX + RandomUtil.randomString(PASSWORD_SUFFIX_RANDOM_LENGTH);
    }

    /**
     *  筛查人员用户密码
     *
     * @param phone     手机号码
     * @param idCard    身份证号码
     * @return java.lang.String
     **/
    public static String getScreeningUserPwd(String phone, String idCard) {
        if (StringUtils.isEmpty(phone) || phone.length() < SCREENING_ADMIN_PWD_SUB_LENGTH || StringUtils.isEmpty(idCard) || idCard.length() < SCREENING_ADMIN_PWD_SUB_LENGTH) {
            throw new ValidationException("筛查人员编号长度不够");
        }
        // 手机号码后四位+身份证号后四位，共8位
        return StrUtil.subSuf(phone, -SCREENING_ADMIN_PWD_SUB_LENGTH) + StrUtil.subSuf(idCard, -SCREENING_ADMIN_PWD_SUB_LENGTH);
    }
}
