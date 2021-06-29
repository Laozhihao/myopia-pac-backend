package com.wupol.myopia.business.aggregation.hospital.service;

import com.wupol.myopia.business.core.common.domain.dto.SuggestHospitalDO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.OrgCooperationHospitalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 合作医院
 *
 * @author Simple4H
 */
@Service
public class OrgCooperationHospitalBizService {

    @Resource
    private OrgCooperationHospitalService orgCooperationHospitalService;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private DistrictService districtService;

    /**
     * 封装推荐医院
     *
     * @param screeningOrgId 筛查机构Id
     * @return SuggestHospitalDO
     */
    public SuggestHospitalDO packageSuggestHospital(Integer screeningOrgId) {
        SuggestHospitalDO hospitalDO = new SuggestHospitalDO();
        Integer hospitalId = orgCooperationHospitalService.getSuggestHospital(screeningOrgId);
        if (Objects.isNull(hospitalId)) {
            return hospitalDO;
        }
        Hospital hospital = hospitalService.getById(hospitalId);
        packageHospitalInfo(hospitalDO, hospital);
        return hospitalDO;
    }

    /**
     * 封装医院信息
     *
     * @param suggestHospitalDO 推荐医院
     * @param hospital          医院实体
     */
    public void packageHospitalInfo(SuggestHospitalDO suggestHospitalDO, Hospital hospital) {
        if (Objects.nonNull(hospital.getAvatarFileId())) {
            suggestHospitalDO.setAvatarFile(resourceFileService.getResourcePath(hospital.getAvatarFileId()));
        }
        suggestHospitalDO.setName(hospital.getName());
        // 行政区域名称
        String address = districtService.getAddressByCode(hospital.getProvinceCode(), hospital.getCityCode(),
                hospital.getAreaCode(), hospital.getTownCode());
        if (StringUtils.isNotBlank(address)) {
            suggestHospitalDO.setAddress(address);
        } else {
            suggestHospitalDO.setAddress(districtService.getDistrictName(hospital.getDistrictDetail()));
        }
    }
}
