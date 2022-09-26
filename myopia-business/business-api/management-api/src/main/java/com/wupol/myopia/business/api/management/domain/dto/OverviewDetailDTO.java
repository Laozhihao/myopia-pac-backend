package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2022/2/18 16:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OverviewDetailDTO extends OverviewDTO {

    /**
     * 绑定医院
     */
    private List<HospitalResponseDTO> hospitals;

    /**
     * 绑定筛查机构
     */
    private List<ScreeningOrgResponseDTO> screeningOrganizations;

    /**
     * 绑定筛查机构
     */
    private List<SchoolResponseDTO> schools;

    /**
     * 已绑定的医院数量
     * @return
     */
    @Override
    public Long getHospitalNum() {
        return Objects.nonNull(super.getHospitalNum()) ? super.getHospitalNum() : CollectionUtils.isEmpty(hospitals) ? 0 : hospitals.size();
    }

    /**
     * 已绑定的筛查机构数量
     * @return
     */
    @Override
    public Long getScreeningOrganizationNum() {
        return Objects.nonNull(super.getScreeningOrganizationNum()) ? super.getScreeningOrganizationNum() : CollectionUtils.isEmpty(screeningOrganizations) ? 0 : screeningOrganizations.size();
    }

    @Override
    public Long getSchoolNum() {
        if (Objects.nonNull(super.getSchoolNum())) {
            return super.getSchoolNum();
        }
        return CollectionUtils.isEmpty(schools) ? 0L : schools.size();
    }

}
