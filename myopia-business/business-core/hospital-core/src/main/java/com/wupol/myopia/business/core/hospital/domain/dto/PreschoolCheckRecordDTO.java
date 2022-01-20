package com.wupol.myopia.business.core.hospital.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.business.core.hospital.domain.interfaces.HasParentInfoInterface;
import com.wupol.myopia.business.core.hospital.domain.model.BaseValue;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/1/4 20:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PreschoolCheckRecordDTO extends PreschoolCheckRecord implements HasParentInfoInterface {

    /**
     * 学号
     */
    private String sno;
    /**
     * 是否有新生儿身份证
     */
    private Boolean hasIdCard;
    /**
     * id信息
     */
    private String idCard;
    /**
     * 家长名称
     */
    private String parentName;
    /**
     * 家长联系方式
     */
    private String parentPhone;
    /**
     * 医院学生ID
     */
    private Integer hospitalStudentId;
    /**
     * 学生名称
     */
    private String studentName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 编号
     */
    private String recordNo;

    /**
     * 就诊医院
     */
    private String hospitalName;

    /**
     * 医生id集，字符串，如1,2,3
     */
    @JsonIgnore
    private String doctorIdsStr;

    /**
     * 医生id集
     */
    @JsonIgnore
    private Set<Integer> doctorIds;

    /**
     * 医师
     */
    private String doctorsName;

    /**
     * 接诊机构
     */
    private String toHospital;

    /**
     * 未做专项检查
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BaseValue> specialMedical;

    /**
     * 初筛异常项目
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BaseValue> diseaseMedical;

    /**
     * 转诊结论
     */
    private String referralConclusion;

    /**
     * 转诊状态[0 待就诊；1 已接诊]
     */
    private Integer referralStatus;

    /**
     * 检查时年龄
     */
    private String createTimeAge;
    /**
        * 家庭信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FamilyInfoVO familyInfo;

    /**
     * 当前用户各月龄段检查情况
     */
    private List<MonthAgeStatusDTO> ageStageStatusList;

    /**
     * 检查后转诊id
     */
    private Integer toReferralId;

    /**
     * 回执单id
     */
    private Integer receiptId;

    /**
     * 转诊后-转诊单
     */
    private ReferralRecord toReferral;

    /**
     * 填充doctorId集
     * @param doctorIdsStr
     * @return
     */
    public PreschoolCheckRecordDTO setDoctorIdsStr(String doctorIdsStr) {
        this.doctorIdsStr = doctorIdsStr;
        this.doctorIds = StringUtils.isBlank(doctorIdsStr) ? new HashSet<>() :
            Arrays.stream(doctorIdsStr.split(","))
                    .filter(x -> !"null".equalsIgnoreCase(x))
                    .map(doctorIdStr -> Integer.valueOf(doctorIdStr)).collect(Collectors.toSet());
        return this;
    }

}
