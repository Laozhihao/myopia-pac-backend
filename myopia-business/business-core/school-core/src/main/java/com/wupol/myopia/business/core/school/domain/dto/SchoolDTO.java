package com.wupol.myopia.business.core.school.domain.dto;

import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/02/16/17:37
 * @Description:
 */
@Data
public class SchoolDTO extends School {

    private List<SchoolGradeDTO> gradeDTOs;
}
