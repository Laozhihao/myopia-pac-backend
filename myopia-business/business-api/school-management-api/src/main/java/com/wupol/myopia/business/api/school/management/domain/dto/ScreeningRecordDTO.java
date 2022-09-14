package com.wupol.myopia.business.api.school.management.domain.dto;

import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 筛查记录查询实体
 *
 * @author hang.yuan 2022/9/13 20:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningRecordDTO extends PageRequest {
    /**
     * studentId
     */
    private Integer studentId;
}
