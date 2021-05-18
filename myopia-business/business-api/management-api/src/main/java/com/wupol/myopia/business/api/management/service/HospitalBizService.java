package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 医院
 *
 * @author Simple4H
 */
@Service
public class HospitalBizService {

    @Resource
    private HospitalService hospitalService;
    @Resource
    private HospitalAdminService hospitalAdminService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private DistrictService districtService;
    @Resource
    private GovDeptService govDeptService;

    /**
     * 更新医院信息
     *
     * @param hospital 医院实体类
     * @return 医院实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public HospitalResponseDTO updateHospital(Hospital hospital) {

        if (hospitalService.checkHospitalName(hospital.getName(), hospital.getId())) {
            throw new BusinessException("医院名字重复，请确认");
        }

        HospitalResponseDTO response = new HospitalResponseDTO();
        Hospital checkHospital = hospitalService.getById(hospital.getId());

        // 医院管理员
        HospitalAdmin admin = hospitalAdminService.getByHospitalId(hospital.getId());

        // 更新OAuth账号
        schoolService.updateOAuthName(admin.getUserId(), hospital.getName());

        // 名字更新
        if (!StringUtils.equals(checkHospital.getName(), hospital.getName())) {
            response.setUsername(hospital.getName());
        }

        hospitalService.updateById(hospital);
        Hospital h = hospitalService.getById(hospital.getId());
        BeanUtils.copyProperties(h, response);
        response.setDistrictName(districtService.getDistrictName(h.getDistrictDetail()));
        // 行政区域名称
        response.setAddressDetail(districtService.getAddressDetails(
                h.getProvinceCode(), h.getCityCode(), h.getAreaCode(), h.getTownCode(), h.getAddress()));
        return response;
    }

    /**
     * 获取医院列表
     *
     * @param pageRequest 分页
     * @param query       请求入参
     * @param govDeptId   部门id
     * @return IPage<Hospital> {@link IPage}
     */
    public IPage<HospitalResponseDTO> getHospitalList(PageRequest pageRequest, HospitalQuery query, Integer govDeptId) {
        IPage<HospitalResponseDTO> hospitalListsPage = hospitalService.getHospitalListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId), query.getName(), query.getType(),
                query.getKind(), query.getLevel(), query.getDistrictId(), query.getStatus());

        List<HospitalResponseDTO> records = hospitalListsPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return hospitalListsPage;
        }
        records.forEach(h -> {
            // 详细地址
            h.setAddressDetail(districtService.getAddressDetails(
                    h.getProvinceCode(), h.getCityCode(), h.getAreaCode(), h.getTownCode(), h.getAddress()));

            // 行政区域名称
            h.setDistrictName(districtService.getDistrictName(h.getDistrictDetail()));
        });
        return hospitalListsPage;
    }
}
