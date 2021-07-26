package com.wupol.myopia.business.core.system.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 行政区域表
 *
 * @Author jacob
 * @Date 2021-03-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_big_screen_map",autoResultMap = true)
public class BigScreenMap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 行政区ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 行政区名称
     */
    @TableField("district_Id")
    private Integer districtId;

    /**
     * json
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object json;

    /**
     * 城市的经纬度位置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<Integer, List<Double>> cityCenterLocation;

    /**
     * 上级行政区代码（省级统一为100000000000）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
