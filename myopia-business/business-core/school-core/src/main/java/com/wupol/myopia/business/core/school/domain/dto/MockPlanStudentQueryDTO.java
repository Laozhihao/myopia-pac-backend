package com.wupol.myopia.business.core.school.domain.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Null;
import java.util.Date;
import java.util.List;

/**
 * @Classname MockPlanStudentQueryDTO
 * @Description
 * @Date 2022/2/22 5:03 下午
 * @Author Jacob
 * @Version
 */
@Data
public class MockPlanStudentQueryDTO {

    /**
     * 护照
     */
    private String passportLike;
    /**
     * 学号
     */
    private String snoLike;
    /**
     * 姓名
     */
    private String nameLike;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startScreeningTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endScreeningTime;
    /**
     * 手机号
     */
    private String phoneLike;
    /**
     * 学校名称
     */
    private String schoolNameLike;
    /**
     * 筛查机构名称
     */
    private String screeningOrgNameLike;
    /**
     * 性别 0-男 1-女
     */
    @Range(min = 0, max = 1)
    private Integer gender;
    /**
     * 机构id(方便service入参, 不接受controller入参)
     */
    @Null
    private List<Integer> screeningOrgIds;
    /**
     * 身份证或者passport
     */
    private String idCardOrPassportLike;
}
