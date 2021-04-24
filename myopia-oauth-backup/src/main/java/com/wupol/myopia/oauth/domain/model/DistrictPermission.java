package com.wupol.myopia.oauth.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 行政区权限表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("o_district_permission")
public class DistrictPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 行政区级别：0-筛查机构、1-省、2-市、3-区县、4-镇乡
     */
    private Integer districtLevel;

    /**
     * 权限资源ID
     */
    private Integer permissionId;


}
