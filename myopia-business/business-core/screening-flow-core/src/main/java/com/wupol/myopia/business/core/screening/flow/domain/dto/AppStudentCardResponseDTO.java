package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCardResponseVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生档案卡实体类
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AppStudentCardResponseDTO {

    /**
     * 学生档案卡实体类
     */
    private List<StudentCardResponseVO> studentCardResponseVO;

    /**
     * 1-默认 2-海口
     */
    private Integer templateId;
}
