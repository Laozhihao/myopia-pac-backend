package com.wupol.myopia.migrate.domain.model;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.RegExpUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 视力筛查
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_student_eye")
public class SysStudentEye implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "eye_id", type = IdType.AUTO)
    private String eyeId;

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生手机号
     */
    private String studentPhone;

    /**
     * 学生身份证号
     */
    private String studentIdcard;

    /**
     * 学术性别
     */
    private String studentSex;

    /**
     * 学术出生日期
     */
    private String studentBirthday;

    /**
     * 省份
     */
    private String studentProvince;

    /**
     * 城市
     */
    private String studentCity;

    /**
     * 区域
     */
    private String studentRegion;

    /**
     * 学校
     */
    private String schoolName;

    /**
     * 拼接名称
     */
    private String splicing;

    /**
     * 年级
     */
    private String schoolGrade;

    /**
     * 班级
     */
    private String schoolClazz;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 学校ID
     */
    private String schoolId;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 筛查人员ID
     */
    private String userId;

    /**
     * 左眼球镜
     */
    private String lSph;

    /**
     * 左眼柱镜
     */
    private String lCyl;

    /**
     * 左眼轴位
     */
    private String lAxial;

    /**
     * 右眼球镜
     */
    private String rSph;

    /**
     * 右眼镜柱
     */
    private String rCyl;

    /**
     * 右眼轴位
     */
    private String rAxial;

    /**
     * 左眼裸视力
     */
    private String lLsl;

    /**
     * 右眼裸视力
     */
    private String rLsl;

    /**
     * 左眼矫正视力
     */
    private String lJzsl;

    /**
     * 右眼矫正视力
     */
    private String rJzsl;

    /**
     * 左眼串镜
     */
    private String lLcj;

    /**
     * 右眼串镜
     */
    private String rLcj;

    /**
     * 左眼屈光
     */
    private String lQg;

    /**
     * 右眼屈光
     */
    private String rQg;

    /**
     * 左眼近视/远视度数
     */
    private String lSe;

    /**
     * 右眼近视/远视度数
     */
    private String rSe;

    /**
     * 左边眼轴
     */
    private String lYz;

    /**
     * 右眼眼轴
     */
    private String rYz;

    /**
     * 带何种眼镜
     */
    private String glasses;

    /**
     * 眼位
     */
    private String positive;

    /**
     * 眼病(左/右)
     */
    private String diseaseEye;

    /**
     * 眼病(左眼)
     */
    private String lDisease;

    /**
     * 眼病(右眼)
     */
    private String rDisease;

    /**
     * 等效球镜（左眼）
     */
    private String lDxsph;

    /**
     * 等效球镜（右眼）
     */
    private String rDxsph;

    /**
     * 是否近视（0：否1：是）
     */
    private Integer isMyopia;

    /**
     * 视力低下情况（1 轻度，2 中度，3重度）
     */
    private Integer low;

    /**
     * 近视情况（1 轻度，2 中度，3重度）
     */
    private Integer myopia;

    /**
     * 视力筛查人员ID
     */
    private String visionUserId;

    /**
     * 电脑验光筛查人员ID
     */
    private String optometryUserId;

    /**
     * 生物测量筛查人员ID
     */
    private String biologyUserId;

    /**
     * 其他眼病筛查人员ID
     */
    private String diseaseUserId;

    /**
     * 电脑验光数据编号
     */
    private String no;

    /**
     * 验光数据时间
     */
    private String printTime;

    /**
     * 近视预警等级（1：低风险，2：中风险，3：高风险，4无风险，5：数据异常）
     */
    private Integer myopiaWarningLevel;

    /**
     * 远视储备（0：无，1：有，2：数据异常）
     */
    private Integer hyperopiaReserve;

    /**
     * 远视预警等级（1：低风险，2：中风险，3：高风险，4无风险，5：数据异常）
     */
    private Integer hyperopiaWarningLevel;

    /**
     * 型号
     */
    private String model;

    /**
     * 矫正情况（0：未矫正，1：足矫，2：欠矫）
     */
    private Integer sectionCorrect;

    /**
     * 筛查次数
     */
    private Long times;

    /**
     * 裸视力预警等级(0异常预警，1视力正常，2一级预警，3二级预警，4三级预警)
     */
    private Integer lslWarningLevel;

    /**
     * 散光预警等级（0异常预警，1视力正常，2一级预警，3二级预警，4三级预警）
     */
    private Integer astigmatismWarningLevel;

    /**
     * 散光程度（0异常预警，1视力正常，2轻度散光，3中度散光，4重度散光）
     */
    private Integer astigmatismLevel;

    /**
     * 学校次数
     */
    private Long schoolTimes;

    /**
     * 是否异常（0：否1：是）
     */
    private Integer isAbnormal;

    /**
     * 自定义眼病
     */
    private String diyDisease;

    /**
     * 转为Map
     *
     * @return java.util.Map<java.lang.Integer,java.lang.String>
     **/
    public Map<Integer, String> convertToMap() {
        Map<Integer, String> studentInfoMap = new HashMap<>(8);
        studentInfoMap.put(ImportExcelEnum.NAME.getIndex(), getStudentName());
        studentInfoMap.put(ImportExcelEnum.GENDER.getIndex(), GenderEnum.UNKNOWN.type.equals(GenderEnum.getType(getStudentSex())) ? GenderEnum.UNKNOWN.desc : getStudentSex());
        studentInfoMap.put(ImportExcelEnum.GRADE.getIndex(), getSchoolGrade());
        studentInfoMap.put(ImportExcelEnum.CLASS.getIndex(), getSchoolClazz());
        studentInfoMap.put(ImportExcelEnum.PHONE.getIndex(), PhoneUtil.isPhone(getStudentPhone()) ? getStudentPhone() : "");
        // 有身份证就不必传出生日期
        if (SysStudentEye.isValidIdCard(getStudentIdcard())) {
            studentInfoMap.put(ImportExcelEnum.ID_CARD.getIndex(), getStudentIdcard().toUpperCase());
        } else if (StringUtils.isNotBlank(studentBirthday) && RegExpUtil.isDate(studentBirthday)){
            studentInfoMap.put(ImportExcelEnum.BIRTHDAY.getIndex(), RegExpUtil.convertDate(studentBirthday));
        }
        return studentInfoMap;
    }

    public static boolean isValidIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        if (!IdcardUtil.isValidCard(idCard)) {
            return false;
        }
        Date birthDay = IdCardUtil.getBirthDay(idCard);
        try {
            DateUtil.checkBirthday(birthDay);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
