package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description
 * @Date 2021/3/7 20:46
 * @Author by Jacob
 */
@NoArgsConstructor
@Data
public class BigScreeningVO {


    private String title;
    /**
     * screeningTitle
     */
    private String screeningTitle;
    /**
     * screeningStartTime
     */
    private Date screeningStartTime;
    /**
     * screeningEndTime
     */
    private Date screeningEndTime;
    /**
     * validDataNum
     */
    private Long validDataNum;
    private Object realScreening;
    /**
     * lowVision
     */
//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private LowVisionDTO lowVision;
    private Object lowVision;
    /**
     * myopia
     */
    //   @TableField(typeHandler = JacksonTypeHandler.class)
    // private MyopiaDTO myopia;
    private Object myopia;
    /**
     * ametropia
     */
/*    @TableField(typeHandler = JacksonTypeHandler.class)
    private AmetropiaDTO ametropia;
    */

    private Object ametropia;
    /**
     * focusOjects
     */
/*    @TableField(typeHandler = JacksonTypeHandler.class)
    private FocusOjectsDTO focusObjects;   */
    private Object focusObjects;
    /**
     * avgVision
     */
/*    @TableField(typeHandler = JacksonTypeHandler.class)
    private AvgVisionDTO avgVision;    */

    private Object avgVision;
    /**
     * mapData
     */
    private Object mapData;
}
