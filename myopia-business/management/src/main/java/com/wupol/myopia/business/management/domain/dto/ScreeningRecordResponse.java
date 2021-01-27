package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 筛查端-记录详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningRecordResponse {

    private Integer schoolCount;

    private Integer staffCount;

    private List<String> staffName;

    private List<RecordDetails> details;
}
