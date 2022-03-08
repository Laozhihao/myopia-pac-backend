package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalReportRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewHospital;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.OverviewHospitalService;
import com.wupol.myopia.business.core.screening.organization.service.OverviewService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private DistrictService districtService;
    @Resource
    private GovDeptService govDeptService;
    @Resource
    private ResourceFileService resourceFileService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private MedicalReportService medicalReportService;
    @Resource
    private StudentBizService studentBizService;
    @Autowired
    private OverviewHospitalService overviewHospitalService;
    @Autowired
    private OverviewService overviewService;

    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveHospital(Hospital hospital, CurrentUser user) {
        UsernameAndPasswordDTO usernameAndPasswordDTO = hospitalService.saveHospital(hospital);
        if (user.isOverviewUser()) {
            // 总览机构：保存总览机构-医院关系，更新缓存信息
            overviewHospitalService.save(new OverviewHospital().setOverviewId(user.getOrgId()).setHospitalId(hospital.getId()));
            overviewService.removeOverviewCache(user.getOrgId());
        }
        return usernameAndPasswordDTO;
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
        // 1.更新医院
        District district = districtService.getById(hospital.getDistrictId());
        hospital.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        Hospital oldHospital = hospitalService.getById(hospital.getId());
        hospitalService.updateById(hospital);
        // 2.更新医院管理员和医生用户的账号权限
        updateAdminAndDoctorAccountPermission(hospital, oldHospital.getAssociateScreeningOrgId());
        // 更新医院管理员用户名称
        if (StringUtils.isNotBlank(hospital.getName()) && (!hospital.getName().equals(oldHospital.getName()))) {
            oauthServiceClient.updateUserRealName(hospital.getName(), hospital.getId(), SystemCode.MANAGEMENT_CLIENT.getCode(),
                    UserType.HOSPITAL_ADMIN.getType());
        }
        if (Objects.nonNull(hospital.getStatus())) {
            // 同步到oauth机构状态
            oauthServiceClient.updateOrganization(new Organization(hospital.getId(), SystemCode.MANAGEMENT_CLIENT,
                    UserType.HOSPITAL_ADMIN, hospital.getStatus()));
        }
        // 3.返回最新消息
        Hospital newHospital = hospitalService.getById(hospital.getId());
        HospitalResponseDTO response = new HospitalResponseDTO();
        BeanUtils.copyProperties(newHospital, response);
        response.setDistrictName(districtService.getDistrictName(newHospital.getDistrictDetail()));
        // 行政区域名称
        response.setAddressDetail(districtService.getAddressDetails(newHospital.getProvinceCode(), newHospital.getCityCode(), newHospital.getAreaCode(), newHospital.getTownCode(), newHospital.getAddress()));
        if (Objects.nonNull(hospital.getAvatarFileId())) {
            response.setAvatarUrl(resourceFileService.getResourcePath(hospital.getAvatarFileId()));
        }
        return response;
    }

    /**
     * 获取医院列表
     *
     * @param pageRequest 分页
     * @param query       请求入参
     * @param user        登录用户
     * @return IPage<Hospital> {@link IPage}
     */
    public IPage<HospitalResponseDTO> getHospitalList(PageRequest pageRequest, HospitalQuery query, CurrentUser user) {
        List<Integer> govOrgIds = new ArrayList<>();
        if (user.isGovDeptUser()) {
            govOrgIds = govDeptService.getAllSubordinate(user.getOrgId());
        }
        IPage<HospitalResponseDTO> hospitalListsPage = hospitalService.getHospitalListByCondition(pageRequest.toPage(), govOrgIds, query);

        List<HospitalResponseDTO> records = hospitalListsPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return hospitalListsPage;
        }
        packageHospitalDTO(records);
        return hospitalListsPage;
    }

    private void packageHospitalDTO(List<HospitalResponseDTO> records) {
        List<Integer> associateScreeningOrgIdList = records.stream().map(Hospital::getAssociateScreeningOrgId).collect(Collectors.toList());
        List<ScreeningOrganization> screeningOrganizationList = screeningOrganizationService.getByIds(associateScreeningOrgIdList);
        Map<Integer, ScreeningOrganization> screeningOrganizationMap = screeningOrganizationList.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
        records.forEach(h -> {
            // 详细地址
            h.setAddressDetail(districtService.getAddressDetails(
                    h.getProvinceCode(), h.getCityCode(), h.getAreaCode(), h.getTownCode(), h.getAddress()));

            // 行政区域名称
            h.setDistrictName(districtService.getDistrictName(h.getDistrictDetail()));

            // 头像
            if (Objects.nonNull(h.getAvatarFileId())) {
                h.setAvatarUrl(resourceFileService.getResourcePath(h.getAvatarFileId()));
            }

            // 关联筛查机构名称
            if (Objects.nonNull(h.getAssociateScreeningOrgId())) {
                ScreeningOrganization screeningOrganization = screeningOrganizationMap.get(h.getAssociateScreeningOrgId());
                h.setAssociateScreeningOrgName(screeningOrganization.getName());
            }
        });
    }


    /**
     * 筛查机构合作医院列表查询
     *
     * @param name    名称
     * @param codePre 代码前缀
     * @return IPage<HospitalResponseDTO>
     */
    public List<HospitalResponseDTO> getHospitalByName(String name, Integer codePre) {
        return hospitalService.getHospitalByName(name, codePre);
    }


    /**
     * 学校管理员用户账号列表
     *
     * @param hospitalId 学校Id
     * @return List<OrgAccountListDTO>
     */
    public List<OrgAccountListDTO> getAccountList(Integer hospitalId) {
        List<OrgAccountListDTO> accountList = new LinkedList<>();
        List<HospitalAdmin> hospitalAdminList = hospitalAdminService.findByList(new HospitalAdmin().setHospitalId(hospitalId));
        if (CollectionUtils.isEmpty(hospitalAdminList)) {
            return accountList;
        }
        List<Integer> userIds = hospitalAdminList.stream().map(HospitalAdmin::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        hospitalAdminList.forEach(adminUser -> {
            User user = userMap.get(adminUser.getUserId());
            OrgAccountListDTO account = new OrgAccountListDTO();
            account.setUserId(adminUser.getUserId());
            account.setOrgId(hospitalId);
            account.setUsername(user.getUsername());
            account.setStatus(user.getStatus());
            accountList.add(account);
        });
        return accountList;
    }

    /**
     * 添加学校管理员账号账号
     *
     * @param hospitalId 学校ID
     * @return UsernameAndPasswordDTO
     */
    public UsernameAndPasswordDTO addHospitalAdminUserAccount(Integer hospitalId) {
        Hospital hospital = hospitalService.getById(hospitalId);
        if (Objects.isNull(hospital)) {
            throw new BusinessException("不存在该学校");
        }
        // 获取该筛查机构已经有多少个账号
        List<HospitalAdmin> adminList = hospitalAdminService.findByList(new HospitalAdmin().setHospitalId(hospitalId));
        if (CollectionUtils.isEmpty(adminList)) {
            throw new BusinessException("数据异常，无主账号");
        }

        // 获取主账号的账号名称
        HospitalAdmin hospitalAdmin = adminList.stream().sorted(Comparator.comparing(HospitalAdmin::getCreateTime)).collect(Collectors.toList()).get(0);
        String mainUsername = oauthServiceClient.getUserDetailByUserId(hospitalAdmin.getUserId()).getUsername();
        String username;
        if (adminList.size() < 10) {
            username = mainUsername + "0" + adminList.size();
        } else {
            username = mainUsername + adminList.size();
        }
        return hospitalService.generateAccountAndPassword(hospital, username, hospital.getAssociateScreeningOrgId());
    }

    /**
     * 更新医院管理员和医生用户的账号权限
     *
     * @param newHospital                新的医院信息
     * @param oldAssociateScreeningOrgId 旧关联筛查机构ID
     * @return void
     **/
    private void updateAdminAndDoctorAccountPermission(Hospital newHospital, Integer oldAssociateScreeningOrgId) {
        Integer newAssociateScreeningOrgId = newHospital.getAssociateScreeningOrgId();
        // 医院管理员(关联筛查机构有变动才更新)
        // 之前没有绑定，现在绑定
        if ((Objects.isNull(oldAssociateScreeningOrgId) && Objects.nonNull(newAssociateScreeningOrgId))) {
            oauthServiceClient.addHospitalUserAssociatedScreeningOrgAdminRole(newHospital.getId(), newAssociateScreeningOrgId);
        // 之前有绑定，现在改绑或解绑
        } else if (Objects.nonNull(oldAssociateScreeningOrgId) && !oldAssociateScreeningOrgId.equals(newAssociateScreeningOrgId)){
            oauthServiceClient.removeHospitalUserAssociatedScreeningOrgAdminRole(newHospital.getId(), oldAssociateScreeningOrgId);
            if (Objects.nonNull(newAssociateScreeningOrgId)) {
                oauthServiceClient.addHospitalUserAssociatedScreeningOrgAdminRole(newHospital.getId(), newAssociateScreeningOrgId);
            }
        }
        // 医生用户
        if (Objects.nonNull(newHospital.getServiceType())) {
            oauthServiceClient.updateHospitalRole(newHospital.getId(), newHospital.getServiceType());
        }
    }

    /**
     * 获取医院就诊列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  医院就诊报告DTO
     * @return List<MedicalReportDO>
     */
    public IPage<ReportAndRecordDO> getReportList(PageRequest pageRequest, HospitalReportRequestDTO requestDTO) {
        IPage<ReportAndRecordDO> pageReport = medicalReportService.getByHospitalId(pageRequest, requestDTO);
        List<ReportAndRecordDO> records = pageReport.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageReport;
        }
        studentBizService.packageReportInfo(records);
        return pageReport;
    }
}
