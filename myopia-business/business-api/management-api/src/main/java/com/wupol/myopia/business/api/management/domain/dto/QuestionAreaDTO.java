package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.common.domain.model.District;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 问卷区域
 *
 * @author xz
 */
@Getter
@Setter
public class QuestionAreaDTO {
    private List<District> districts;

    private Integer defaultAreaId;

    private String defaultAreaName;
}
