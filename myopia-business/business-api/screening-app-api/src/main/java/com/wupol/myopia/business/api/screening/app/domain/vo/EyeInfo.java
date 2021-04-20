package com.wupol.myopia.business.api.screening.app.domain.vo;

import lombok.Data;

@Data
public class EyeInfo {
    /**
     * 视力检查项
     */
    private String name;
    /**
     * 初测右
     */
    private String firstRight;
    /**
     * 初测左
     */
    private String firstLeft;
    /**
     * 复测右
     */
    private String reviewRight;
    /**
     * 复测左
     */
    private String reviewLeft;
    /**
     * 左眼是否合格（0-异常，1-合格，2-不合格）
     */
    private Integer leftQualified;
    /**
     * 左眼是否合格（0-异常，1-合格，2-不合格）
     */
    private Integer rightQualified;

    public EyeInfo(String name, String firstRight, String firstLeft, String reviewRight, String reviewLeft, Integer leftQualified, Integer rightQualified) {
        this.name = name;
        this.firstLeft = firstLeft==null?"":firstLeft;
        this.firstRight = firstRight==null?"":firstRight;
        this.reviewRight = reviewRight==null?"":reviewRight;
        this.reviewLeft = reviewLeft==null?"":reviewLeft;
        this.leftQualified = leftQualified==null?0:leftQualified;
        this.rightQualified = rightQualified==null?0:rightQualified;
    }


}
