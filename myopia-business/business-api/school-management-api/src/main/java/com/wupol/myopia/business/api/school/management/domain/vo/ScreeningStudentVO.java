package com.wupol.myopia.business.api.school.management.domain.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.aggregation.screening.domain.vos.ScreeningStudentListVO;
import lombok.Data;

import java.io.Serializable;

/**
 * 筛查学生
 *
 * @author hang.yuan 2022/9/22 14:57
 */
@Data
public class ScreeningStudentVO implements Serializable {

    /**
     * 是否有筛查学生
     */
    private Boolean hasScreeningStudent;

    /**
     * 分页数据
     */
    private IPage<ScreeningStudentListVO> pageData;
}
