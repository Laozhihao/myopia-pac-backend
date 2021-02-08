package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生档案卡返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentScreeningResultResponseDTO {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 详情
     */
    private List<StudentScreeningResultItems> items;

}
