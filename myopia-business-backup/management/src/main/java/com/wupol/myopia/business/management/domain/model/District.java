package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wupol.myopia.business.management.interfaces.HasName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 行政区域表
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Data
@Accessors(chain = true)
@TableName("m_district")
public class District implements Serializable, HasName {

    private static final long serialVersionUID = 1L;

    /** 行政区ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 行政区名称 */
    private String name;

    /** 行政区代码 */
    private Long code;

    /** 上级行政区代码（省级统一为100000000） */
    private Long parentCode;

    /** 片区代码 */
    private Integer areaCode;

    /** 监测点代码 */
    private Integer monitorCode;

    @TableField(exist = false)
    private List<District> child;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof District)) return false;
        District district = (District) o;
        return Objects.equals(getId(), district.getId()) && Objects.equals(getName(), district.getName()) && Objects.equals(getCode(), district.getCode()) && Objects.equals(getParentCode(), district.getParentCode()) && Objects.equals(getAreaCode(), district.getAreaCode()) && Objects.equals(getMonitorCode(), district.getMonitorCode()) && Objects.equals(getChild(), district.getChild());
    }

    @Override
    public int hashCode() {
        // 这里不包含child
        return Objects.hash(getId(), getName(), getCode(), getParentCode(), getAreaCode(), getMonitorCode());
    }
}
