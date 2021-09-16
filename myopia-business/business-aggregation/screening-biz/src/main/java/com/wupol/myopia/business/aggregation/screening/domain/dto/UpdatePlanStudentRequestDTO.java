package com.wupol.myopia.business.aggregation.screening.domain.dto;

import com.wupol.myopia.base.util.DateUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * 更新筛查学生学生
 *
 * @author Simple4H
 */
@Data
public class UpdatePlanStudentRequestDTO {

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotNull(message = "年龄不能为空")
    private Integer studentAge;

    private String parentPhone;

    private Date birthday;

    private String sno;

    @NotNull(message = "学生ID不能为空")
    private Integer planStudentId;

    private Integer studentId;

    public Date getBirthday() {
        if (Objects.nonNull(birthday) || Objects.isNull(studentAge)) {
            return birthday;
        }
        LocalDate date= LocalDate.of(DateUtil.getYear(new Date()) - studentAge,1,1);
        return DateUtil.toDate(date);
    }
}
