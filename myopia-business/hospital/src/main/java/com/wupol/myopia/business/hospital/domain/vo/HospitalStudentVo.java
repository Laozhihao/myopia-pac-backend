package com.wupol.myopia.business.hospital.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 医院-学生
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class HospitalStudentVo extends HospitalStudent {
    /** 医院名称 */
    private String hospitalName;
    /** 影像列表 */
    private List<String> imageUrlList;
    /** 最后一次就诊时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastVisitDate;
    /** 就诊次数 */
    private Integer numOfVisits;
}
