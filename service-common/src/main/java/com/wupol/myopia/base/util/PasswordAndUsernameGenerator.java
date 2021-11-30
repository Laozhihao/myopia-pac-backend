package com.wupol.myopia.base.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;
import java.util.Date;

/**
 * 密码生成器
 * 需求见：https://vistelab.sharepoint.com/:w:/r/sites/msteams_4c1013/_layouts/15/Doc.aspx?sourcedoc=%7B9EC045B8-B6C4-4F0A-AB18-57733870408D%7D&file=JS1.1.010-1%E8%81%94%E5%8A%A8-%E7%AE%A1%E7%90%86%E5%90%8E%E5%8F%B0%E5%B0%B1%E8%AF%8A%E5%88%A4%E6%96%AD%E5%92%8C%E8%B4%A6%E5%8F%B7%E5%90%8D%E7%A7%B0%E5%8F%98%E6%9B%B4%E5%8A%9F%E8%83%BD.docx&action=default&mobileredirect=true&cid=9936c55c-3d63-4be4-8dde-07c6ab354a12
 *
 * @Author HaoHao
 * @Date 2020/12/29
 **/
@UtilityClass
public class PasswordAndUsernameGenerator {

    /** 筛查人员密码截取长度 */
    private final int SCREENING_ADMIN_PWD_SUB_LENGTH = 4;
    /** 密码后缀随机数长度 */
    private final int PASSWORD_SUFFIX_RANDOM_LENGTH = 11;

    /** 管理端用户密码前缀 */
    private final String MANAGEMENT_USER_PWD_PREFIX = "g";
    /** 学校端管理员用户密码前缀 */
    private final String SCHOOL_ADMIN_PWD_PREFIX = "x";
    /** 医院端管理员用户密码前缀 */
    private final String HOSPITAL_ADMIN_PWD_PREFIX = "y";
    /** 筛查机构管理端的管理员用户密码前缀 */
    private final String SCREENING_ORG_ADMIN_PWD_PREFIX = "s";

    /** 用户名 */
    private final String USERNAME = "jsfk%s%d";

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
        return SCHOOL_ADMIN_PWD_PREFIX + RandomUtil.randomNumbers(PASSWORD_SUFFIX_RANDOM_LENGTH);
    }

    /**
     * 医院端管理员用户密码
     *
     * @return java.lang.String
     **/
    public static String getHospitalAdminPwd() {
        // 开头字母y + 11位字母或数字
        return HOSPITAL_ADMIN_PWD_PREFIX + RandomUtil.randomNumbers(PASSWORD_SUFFIX_RANDOM_LENGTH);
    }

    /**
     * 筛查机构管理端的管理员用户密码
     *
     * @return java.lang.String
     **/
    public static String getScreeningAdminPwd() {
        // 开头字母y + 11位字母或数字
        return SCREENING_ORG_ADMIN_PWD_PREFIX + RandomUtil.randomNumbers(PASSWORD_SUFFIX_RANDOM_LENGTH);
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

    /**
     * 获取学校端管理员用户名
     *
     * @param sequence 序号
     * @return java.lang.String
     **/
    public static String getSchoolAdminUserName(int sequence) {
        return String.format(USERNAME, SCHOOL_ADMIN_PWD_PREFIX, sequence);
    }

    /**
     * 获取医院端管理员用户名
     *
     * @param sequence 序号
     * @return java.lang.String
     **/
    public static String getHospitalAdminUserName(int sequence) {
        return String.format(USERNAME, HOSPITAL_ADMIN_PWD_PREFIX, sequence);
    }

    /**
     * 获取筛查端管理员用户名
     *
     * @param sequence 序号
     * @return java.lang.String
     **/
    public static String getScreeningOrgAdminUserName(int sequence) {
        return String.format(USERNAME, SCREENING_ORG_ADMIN_PWD_PREFIX, sequence);
    }

    /**
     * 获取医生默认密码
     * @param phone
     * @return
     */
    public static String getDoctorPwd(String phone, Date date) {
        // 手机号码后5位+创建日期的日（01-31），合计7位数
        return StrUtil.subSuf(phone, -SCREENING_ADMIN_PWD_SUB_LENGTH) + DateFormatUtil.format(date, DateFormatUtil.FORMAT_ONLY_DAY);
    }

}
