package com.wupol.myopia.business.api.school.management.domain.dto;

import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 检查列表
 *
 * @author hang.yuan 2022/9/14 00:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CheckListDTO extends PageRequest {
    private Integer studentId;
    private List<Integer> monthAges;
}
