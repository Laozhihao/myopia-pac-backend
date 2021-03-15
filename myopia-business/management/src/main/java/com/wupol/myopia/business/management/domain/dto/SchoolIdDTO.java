package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.School;
import lombok.Data;

/**
 * 筛查app 使用的
  */
@Data
public class SchoolIdDTO extends School {
    private Integer schoolId;
 }
