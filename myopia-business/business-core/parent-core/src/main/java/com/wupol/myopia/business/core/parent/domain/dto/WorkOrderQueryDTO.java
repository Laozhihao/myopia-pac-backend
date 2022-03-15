package com.wupol.myopia.business.core.parent.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 工单查询
 * @Author xjl
 * @Date 2022/3/7
 */
@Data
public class WorkOrderQueryDTO {


    /**
     * 学生姓名
     */
    private String name;

    /**
     * 状态 0-已处理 1-未处理 2-无法处理
     */
    private Integer status;

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
