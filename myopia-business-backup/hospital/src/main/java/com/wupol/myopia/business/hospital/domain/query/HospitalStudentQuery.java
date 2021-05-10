package com.wupol.myopia.business.hospital.domain.query;

import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 医院的学生查询
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class HospitalStudentQuery extends HospitalStudent {

    /** 开始日期 */
    private Date startDate;
    /** 结束日期 */
    private Date endDate;
    /** 模糊搜索的名称 */
    private String nameLike;
    /** 查询的学生id列 */
    private List<Integer> studentIdList;
}
