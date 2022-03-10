package com.wupol.myopia.business.core.parent.domain.dto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.parent.domain.model.WorkOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 工单查询
 * @Author xjl
 * @Date 2022/3/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkOrderQueryDTO extends WorkOrder {


    /**
     * 护照或身份证号
     */
    private String idCardOrPassport;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 学校ids
     */
    private List<Integer> schoolIds;


    /**
     * 申请起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 申请结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;


}
