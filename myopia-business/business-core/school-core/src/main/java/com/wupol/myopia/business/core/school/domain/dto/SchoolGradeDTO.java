package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/02/16/17:38
 * @Description:
 */
@Data
public class SchoolGradeDTO extends SchoolGrade {
    private String classNames;
//    private List<SchoolClassDTO> schoolClassDTOs;
}
