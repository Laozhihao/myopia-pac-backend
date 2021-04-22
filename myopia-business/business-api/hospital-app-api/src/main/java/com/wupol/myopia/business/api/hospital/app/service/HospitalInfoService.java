package com.wupol.myopia.business.api.hospital.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医院-信息
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalInfoService {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalAdminService hospitalAdminService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;

    /**
     * 获取医院信息
     * @param hospitalId 医院id
     * @return
     */
    public Map<String, Object> getHospitalInfo(Integer hospitalId) throws IOException {
        Map<String, Object> map = new HashMap<>(3);
        // 累计就诊的人数
        map.put("totalMedicalPersonCount", medicalRecordService.count(new MedicalRecord().setHospitalId(hospitalId)));
        map.put("name", hospitalService.getById(hospitalId).getName());
        return map;
    }


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

        // 名字更新重置密码
        if (!StringUtils.equals(checkHospital.getName(), hospital.getName())) {
            response.setUpdatePassword(Boolean.TRUE);
            response.setUsername(hospital.getName());
            // 重置密码
            String password = PasswordGenerator.getHospitalAdminPwd();
            oauthService.resetPwd(admin.getUserId(), password);
            response.setPassword(password);
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