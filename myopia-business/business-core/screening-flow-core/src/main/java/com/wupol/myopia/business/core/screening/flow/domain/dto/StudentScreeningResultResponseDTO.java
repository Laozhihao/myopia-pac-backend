package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
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
    private Long total;

    private Long pages;

    private Long size;

    private Long current;

    private List<OrderItem> orders;

    private String countId;

    private Long maxLimit;

    private boolean searchCount;

    private boolean optimizeCountSql;

    private boolean hitCount;

    /**
     * 详情
     */
    private List<StudentScreeningResultItemsDTO> records;



}
