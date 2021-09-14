package com.wupol.myopia.business.api.screening.app.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 盲及视力损害分类（等级）
 *
 * @Author HaoHao
 * @Date 2021/9/13
 **/
@Data
public class VisualLossLevelDataDTO implements Serializable {
    /**
     * 左：0~9 级
     */
    private Integer leftVisualLossLevel;

    /**
     * 右：0~9 级
     */
    private Integer rightVisualLossLevel;
}
