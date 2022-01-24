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
public class ScreeningOrganizationStaffVO extends UsernameAndPasswordDTO {
    /**
     * 该机构下筛查人员总人数
     */
    private Integer screeningStaffTotalNum;

    public static ScreeningOrganizationStaffVO parseFromUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        ScreeningOrganizationStaffVO screeningOrganizationVO = new ScreeningOrganizationStaffVO();
        BeanUtils.copyProperties(usernameAndPasswordDTO, screeningOrganizationVO);
        return screeningOrganizationVO;
    }
}
