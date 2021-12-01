package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalAdminService;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.BeanUtils;
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
    @Resource
    private HospitalStudentService hospitalStudentService;

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
        District district = districtService.getById(hospital.getDistrictId());
        hospital.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        hospitalService.updateById(hospital);
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
        if (!user.isPlatformAdminUser()) {
            govOrgIds = govDeptService.getAllSubordinate(user.getOrgId());
        }
        IPage<HospitalResponseDTO> hospitalListsPage = hospitalService.getHospitalListByCondition(pageRequest.toPage(), govOrgIds,
                query.getName(), query.getType(), query.getKind(), query.getLevel(), query.getDistrictId(), query.getStatus());

        List<HospitalResponseDTO> records = hospitalListsPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return hospitalListsPage;
        }
        packageHospitalDTO(records);
        return hospitalListsPage;
    }

    private void packageHospitalDTO(List<HospitalResponseDTO> records) {
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
        hospital.setName(hospital.getName() + "0" + adminList.size());

        // 获取主账号的账号名称
        HospitalAdmin hospitalAdmin = adminList.stream().sorted(Comparator.comparing(HospitalAdmin::getCreateTime)).collect(Collectors.toList()).get(0);
        String mainUsername = oauthServiceClient.getUserDetailByUserId(hospitalAdmin.getUserId()).getUsername();
        String username;
        if (adminList.size() < 10) {
            username = mainUsername + "0" + adminList.size();
        } else {
            username = mainUsername + adminList.size();
        }
        return hospitalService.generateAccountAndPassword(hospital,username);
    }

    /**
     * 获取医院学生
     *
     * @param pageRequest 分页请求
     * @param requestDTO  条件
     * @return IPage<HospitalStudentResponseDTO>
     */
    public IPage<HospitalStudentResponseDTO> getHospitalStudent(PageRequest pageRequest, HospitalStudentRequestDTO requestDTO) {
        return hospitalStudentService.getByList(pageRequest, requestDTO);
    }
}
