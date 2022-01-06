package com.wupol.myopia.business.api.parent.domain.dto;

import com.wupol.myopia.business.api.parent.domain.dos.ScreeningReportDetailDO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import lombok.Getter;
import lombok.Setter;

/**
 * 筛查报告返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningReportResponseDTO {

    /**
     * 详情
     */
    private ScreeningReportDetailDO detail;

    /**
     * 是否新生儿暂无身份证 false-否 true-是
     */
    private Boolean isNewbornWithoutIdCard;

    /**
     * 学生信息
     */
    private StudentDTO studentInfo;
}
