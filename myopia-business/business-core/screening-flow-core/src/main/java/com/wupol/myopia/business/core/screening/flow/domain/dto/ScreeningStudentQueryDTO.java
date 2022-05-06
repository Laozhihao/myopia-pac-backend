package com.wupol.myopia.business.core.screening.flow.domain.dto;


import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.domain.dto.MockPlanStudentQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 学生查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ScreeningStudentQueryDTO extends StudentExtraDTO {
    /** 名称 */
    private String nameLike;
    /** 身份证 */
    private String idCardLike;
    /** 学号 */
    private String snoLike;
    /** 手机号 */
    private String phoneLike;
    /** 导出本地报告数据起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    /** 导出本地报告数据结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startScreeningTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endScreeningTime;

    /**
     * 年级ids 逗号隔开
     */
    private String gradeIds;
    /** 年级ids */
    private List<Integer> gradeList;
    /** idCard列表 */
    private List<String> idCardList;

    /**
     * 视力标签
     */
    private String visionLabels;

    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;
    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 年级ID
     */
    private Integer gradeId;
    /**
     * 班级D
     */
    private Integer classId;
    /**
     * 筛查机构ID
     */
    private Integer screeningOrgId;
    /**
     * 筛查计划ID集合
     */
    private Set<Integer> planIds;

    /**
     * 筛查编号
     */
    private Long screeningCode;

    /**
     * 护照
     */
    private String passportLike;
    /**
     * 学校名称
     */
    private String schoolNameLike;
    /**
     * 身份证或者护照
     */
    private String idCardOrPassportLike;

    /**
     * 0-非人造的、1-人造的
     */
    private Integer artificial;

    /**
     * 是否复测
     */
    private Integer isDoubleScreen;

    /**
     * 筛查类型（0视力筛查，1常见病筛查）
     */
    private Integer screeningType;

    /**
     * 创建ScreeningStudentQueryDTO, 当入参为null时, 返回null;
     * @param mockPlanStudentQueryDTO
     * @return
     */
    public static ScreeningStudentQueryDTO getScreeningStudentQueryDTO(MockPlanStudentQueryDTO mockPlanStudentQueryDTO) {
        if (mockPlanStudentQueryDTO == null) {
            return null;
        }
        //处理下时间
        Date endScreeningTime = mockPlanStudentQueryDTO.getEndScreeningTime();
        if (endScreeningTime != null) {
            //时间过来是 2001-01-01 00:00:00
            //实际上应该增加一天  2001-01-02 00:00:00
            endScreeningTime = DateUtil.offsetDay(endScreeningTime, 1);
        }

        ScreeningStudentQueryDTO screeningStudentQueryDTO = new ScreeningStudentQueryDTO();
        screeningStudentQueryDTO.setPlanIds(mockPlanStudentQueryDTO.getScreeningPlanIds())
                .setStartScreeningTime(mockPlanStudentQueryDTO.getStartScreeningTime())
                .setEndScreeningTime(endScreeningTime)
                .setArtificial(ArtificialStatusConstant.Artificial)
                .setSnoLike(mockPlanStudentQueryDTO.getSnoLike())
                .setNameLike(mockPlanStudentQueryDTO.getNameLike())
                .setPhoneLike(mockPlanStudentQueryDTO.getPhoneLike())
                .setIdCardOrPassportLike(mockPlanStudentQueryDTO.getIdCardOrPassportLike())
                .setPassportLike(mockPlanStudentQueryDTO.getPassportLike())
                .setSchoolNameLike(mockPlanStudentQueryDTO.getSchoolNameLike())
                .setGender(mockPlanStudentQueryDTO.getGender());
        return screeningStudentQueryDTO;
    }
}
