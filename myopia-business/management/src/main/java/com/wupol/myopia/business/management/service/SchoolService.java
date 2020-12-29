package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolDto;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDto;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Resource
    private SchoolStaffService SchoolStaffService;

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private SchoolMapper schoolMapper;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDto saveSchool(School school) {
        if (null == school.getTownCode()) {
            throw new BusinessException("数据异常");
        }
        school.setSchoolNo(generateSchoolNo(school.getTownCode()));
        baseMapper.insert(school);
        return generateAccountAndPassword(school);
    }

    /**
     * 更新学校
     *
     * @param school 学校实体类
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateSchool(School school) {
        return baseMapper.updateById(school);
    }

    /**
     * 删除学校
     *
     * @param id 学校id
     * @return 删除数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedSchool(Integer id) {
        School school = new School();
        school.setId(id);
        school.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(school);
    }

    /**
     * 获取学校列表
     *
     * @param pageRequest 分页
     * @param schoolQuery 请求体
     * @param govDeptId   部门ID
     * @return IPage<SchoolDto> {@link IPage}
     */
    public IPage<SchoolDto> getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery, Integer govDeptId) {
        IPage<SchoolDto> schoolDtoLists = schoolMapper.getSchoolListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId), schoolQuery.getName(),
                schoolQuery.getSchoolNo(), schoolQuery.getType(), schoolQuery.getCode());
        schoolDtoLists.getRecords().forEach(s -> s.setAccountNo("abc"));
        return schoolDtoLists;
    }


    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDto generateAccountAndPassword(School school) {
        String password = PasswordGenerator.getSchoolAdminPwd(school.getSchoolNo());
        String username = school.getName();
//        UserDTO userDTO = new UserDTO();
//        userDTO.setOrgId(school.getGovDeptId());
//        userDTO.setUsername(username);
//        userDTO.setPassword(password);
//        userDTO.setCreateUserId(school.getCreateUserId());
//        userDTO.setSystemCode(SystemCode.SCHOOL_CLIENT.getCode());
//
//        ApiResult apiResult = oauthServiceClient.addUser(userDTO);
//        if (!apiResult.isSuccess()) {
//            throw new BusinessException("创建管理员信息异常");
//        }
//        UserDTO data = (UserDTO) apiResult.getData();
//
//        SchoolStaffService.insertStaff(school.getId(), school.getCreateUserId(), school.getGovDeptId(), data.getId());
        return new UsernameAndPasswordDto(username, password);
    }

    private String generateSchoolNo(Integer code) {
        School school = schoolMapper.getLastSchoolByNo(code);
        if (null == school) {
            return StringUtils.join(code, "001");
        }
        return String.valueOf(Long.parseLong(school.getSchoolNo()) + 1);
    }
}
