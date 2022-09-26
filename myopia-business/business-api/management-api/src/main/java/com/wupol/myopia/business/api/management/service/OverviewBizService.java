package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.util.OverviewConfigUtil;
import com.wupol.myopia.business.api.management.domain.dto.OverviewDetailDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.query.OverviewQuery;
import com.wupol.myopia.business.core.screening.organization.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/2/18 12:05
 */
@Service
public class OverviewBizService {

    @Autowired
    private OverviewService overviewService;

    @Autowired
    private OverviewHospitalService overviewHospitalService;

    @Autowired
    private OverviewScreeningOrganizationService overviewScreeningOrganizationService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OverviewSchoolService overviewSchoolService;

    @Autowired
    private SchoolService schoolService;

    /**
     * 获取总览机构详情
     * @param overviewId
     * @return
     */
    public OverviewDetailDTO getDetail(Integer overviewId) {
        Overview overview = overviewService.getById(overviewId);
        OverviewDetailDTO detail = new OverviewDetailDTO();
        BeanUtils.copyProperties(overview, detail);
        packageBindInfo(detail);
        return detail;
    }

    /**
     * 获取总览机构列表
     *
     * @param pageRequest 分页
     * @param query       请求入参
     * @return IPage<Hospital> {@link IPage}
     */
    public IPage<OverviewDTO> getOverviewList(PageRequest pageRequest, OverviewQuery query) {
        IPage<OverviewDTO> overviewPage = overviewService.getOverviewListByCondition(pageRequest.toPage(), query);
        packageOverviewDTO(overviewPage.getRecords());
        return overviewPage;
    }

    /**
     * 组装数据
     * @param records
     */
    private void packageOverviewDTO(List<OverviewDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return ;
        }
        List<Integer> overviewIds = records.stream().map(OverviewDTO::getId).collect(Collectors.toList());
        Map<Integer, Long> hospitalNumMap = overviewHospitalService.getOverviewHospitalNum(overviewIds);
        Map<Integer, Long> screeningOrganizationNumMap = overviewScreeningOrganizationService.getOverviewScreeningOrganizationNum(overviewIds);
        // 设置绑定的医院数量、筛查机构数量、行政区域名称
        records.forEach(overviewDTO -> {
            overviewDTO.setHospitalNum(hospitalNumMap.getOrDefault(overviewDTO.getId(), 0L));
            overviewDTO.setScreeningOrganizationNum(screeningOrganizationNumMap.getOrDefault(overviewDTO.getId(), 0L));
            overviewDTO.setDistrictName(districtService.getDistrictName(overviewDTO.getDistrictDetail()));
            overviewDTO.setConfigTypeList(OverviewConfigUtil.configTypeList(overviewDTO.getConfigType()));
        });
    }

    /**
     * 补充绑定信息（医院、筛查机构）
     * @param detail
     */
    private void packageBindInfo(OverviewDetailDTO detail) {
        // 设置医院
        List<Integer> hospitalIds = overviewHospitalService.getHospitalIdByOverviewId(detail.getId());
        if (CollectionUtils.isNotEmpty(hospitalIds)) {
            List<Hospital> hospitals = hospitalService.listByIds(hospitalIds);
            detail.setHospitals(hospitals.stream().map(hospital -> {
                HospitalResponseDTO hospitalResponseDTO = new HospitalResponseDTO();
                hospitalResponseDTO.setId(hospital.getId()).setName(hospital.getName());
                hospitalResponseDTO.setDistrictName(districtService.getDistrictName(hospital.getDistrictDetail()));
                return hospitalResponseDTO;
            }).collect(Collectors.toList()));
        }

        // 设置筛查机构
        List<Integer> screeningOrganizationIds = overviewScreeningOrganizationService.getScreeningOrganizationIdByOverviewId(detail.getId());
        if (CollectionUtils.isNotEmpty(screeningOrganizationIds)) {
            List<ScreeningOrganization> screeningOrganizations = screeningOrganizationService.listByIds(screeningOrganizationIds);
            detail.setScreeningOrganizations(screeningOrganizations.stream().map(screeningOrganization -> {
                ScreeningOrgResponseDTO screeningOrgResponseDTO = new ScreeningOrgResponseDTO();
                screeningOrgResponseDTO.setId(screeningOrganization.getId()).setName(screeningOrganization.getName());
                screeningOrgResponseDTO.setDistrictName(districtService.getDistrictName(screeningOrganization.getDistrictDetail()));
                return screeningOrgResponseDTO;
            }).collect(Collectors.toList()));
        }

        // 设置学校
        List<Integer> schoolIds = overviewSchoolService.getSchoolIdByOverviewId(detail.getId());
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            List<School> schools = schoolService.listByIds(schoolIds);
            detail.setSchools(schools.stream().map(school -> {
                SchoolResponseDTO schoolResponseDTO = new SchoolResponseDTO();
                schoolResponseDTO.setId(school.getId())
                        .setName(school.getName());
                schoolResponseDTO.setDistrictName(districtService.getDistrictName(school.getDistrictDetail()));
                return schoolResponseDTO;
            }).collect(Collectors.toList()));
        }
    }

}
