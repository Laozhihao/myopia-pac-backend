package com.wupol.myopia.business.api.management.domain.vo;

import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

/**
 * @Author HaoHao
 * @Date 2022/1/21
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DoctorVO extends UsernameAndPasswordDTO {
    /**
     * 该医院下的医生总人数
     */
    private Integer doctorTotalNum;

    public static DoctorVO parseFromUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        DoctorVO doctorVO = new DoctorVO();
        BeanUtils.copyProperties(usernameAndPasswordDTO, doctorVO);
        return doctorVO;
    }
}
