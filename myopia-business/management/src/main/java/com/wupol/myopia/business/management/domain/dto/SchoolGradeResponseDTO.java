package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 班级返回体
 *
 * @author Simple4H
 */
@Setter
@Getter
public class SchoolGradeResponseDTO {

    private List<SchoolGradeItems> items;
}
