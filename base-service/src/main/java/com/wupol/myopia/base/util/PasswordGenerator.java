package com.wupol.myopia.base.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.base.constant.PwdConstant;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;

/**
 * 密码生成器
 *
 * @Author HaoHao
 * @Date 2020/12/29
 **/
public class PasswordGenerator {

    /** 医院管理员密码截取长度 */
    private static final int HOSPITAL_ADMIN_PWD_SUB_LENGTH = 6;
    /** 学校管理员密码截取长度 */
    private static final int SCHOOL_ADMIN_PWD_SUB_LENGTH = 6;
    /** 筛查人员密码截取长度 */
    private static final int SCREENING_ADMIN_PWD_SUB_LENGTH = 4;
    /** 筛查机构管理员密码截取长度 */
    private static final int ORG_ADMIN_PWD_SUB_LENGTH = 6;

    /**
     * 管理端用户密码
     *
     * @return java.lang.String
     **/
    public static String getManagementUserPwd() {
        // 开头字母j + 7个随机字母或数字
        return PwdConstant.MANAGEMENT_USER_PWD_PREFIX + RandomUtil.randomNumbers(7);
    }

    /**
     * 学校端管理员密码
     *
     * @param schoolNo 学校编号
     * @return java.lang.String
     **/
    public static String getSchoolAdminPwd(String schoolNo) {
        if (StringUtils.isEmpty(schoolNo) || schoolNo.length() < SCHOOL_ADMIN_PWD_SUB_LENGTH) {
            throw new ValidationException("学校编号长度不够");
        }
        // 开头字母x + 学校ID后6位
        return PwdConstant.SCHOOL_USER_PWD_PREFIX + StrUtil.subSuf(schoolNo, -SCHOOL_ADMIN_PWD_SUB_LENGTH);
    }

    /**
     * 医院端管理员用户密码
     * @param hospitalNo 医院编号
     * @return java.lang.String
     **/
    public static String getHospitalAdminPwd(String hospitalNo) {
        if (StringUtils.isEmpty(hospitalNo) || hospitalNo.length() < HOSPITAL_ADMIN_PWD_SUB_LENGTH) {
            throw new ValidationException("医院编号长度不够");
        }
        // 开头字母y + 医院ID后6位
        return PwdConstant.HOSPITAL_USER_PWD_PREFIX + StrUtil.subSuf(hospitalNo, -HOSPITAL_ADMIN_PWD_SUB_LENGTH);
    }

    /**
     * 筛查机构管理员用户密码
     *
     * @param orgNo 医院编号
     * @return java.lang.String
     **/
    public static String getScreeningOrgAdminPwd(String orgNo) {
        if (StringUtils.isEmpty(orgNo) || orgNo.length() < ORG_ADMIN_PWD_SUB_LENGTH) {
            throw new ValidationException("筛查机构编号长度不够");
        }
        // 开头字母s + 筛查ID后6位
        return PwdConstant.ORG_USER_PWD_PREFIX + StrUtil.subSuf(orgNo, -ORG_ADMIN_PWD_SUB_LENGTH);
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
            throw new ValidationException("医院编号长度不够");
        }
        // 手机号码后四位+身份证号后四位，共8位
        return StrUtil.subSuf(phone, -SCREENING_ADMIN_PWD_SUB_LENGTH) + StrUtil.subSuf(phone, -SCREENING_ADMIN_PWD_SUB_LENGTH);
    }

}
