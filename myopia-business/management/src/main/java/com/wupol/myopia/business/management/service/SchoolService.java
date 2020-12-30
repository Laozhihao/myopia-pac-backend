package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolDto;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.SchoolStaff;
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
    private SchoolStaffService schoolStaffService;

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
    public synchronized UsernameAndPasswordDTO saveSchool(School school) {
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
     * 更新状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {

        SchoolStaff staff = schoolStaffService.getStaffBySchoolId(request.getId());
        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(staff.getUserId())
                .setStatus(request.getStatus());
        ApiResult apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("OAuth2 异常");
        }
        School school = new School().setId(request.getId()).setStatus(request.getStatus());
        return schoolMapper.updateById(school);
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
        return schoolMapper.getSchoolListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId), schoolQuery.getName(),
                schoolQuery.getSchoolNo(), schoolQuery.getType(), schoolQuery.getCode());
    }

    /**
     * 重置密码
     *
     * @param id 医院id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        School school = schoolMapper.selectById(id);
        if (null == school) {
            throw new BusinessException("数据异常");
        }
        SchoolStaff staff = schoolStaffService.getStaffBySchoolId(id);
        return resetOAuthPassword(school, staff.getUserId());
    }


    /**
     * 生成账号密码
     *
     * @return UsernameAndPasswordDto 账号密码
     */
    private UsernameAndPasswordDTO generateAccountAndPassword(School school) {
        String password = PasswordGenerator.getSchoolAdminPwd(school.getSchoolNo());
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(school.getId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(school.getCreateUserId())
                .setSystemCode(SystemCode.SCHOOL_CLIENT.getCode());

        ApiResult<UserDTO> apiResult = oauthServiceClient.addAdminUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建管理员信息异常");
        }
        schoolStaffService.insertStaff(school.getId(), school.getCreateUserId(), school.getGovDeptId(), apiResult.getData().getId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 生成学校编号
     *
     * @param code 行政区代码
     * @return 编号
     */
    private String generateSchoolNo(Integer code) {
        School school = schoolMapper.getLastSchoolByNo(code);
        if (null == school) {
            return StringUtils.join(code, "001");
        }
        return String.valueOf(Long.parseLong(school.getSchoolNo()) + 1);
    }

    /**
     * 重置密码
     *
     * @param school 学校
     * @param userId OAuth2 的userId
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(School school, Integer userId) {
        String password = PasswordGenerator.getSchoolAdminPwd(school.getSchoolNo());
        String username = school.getName();

        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username)
                .setPassword(password);
        ApiResult apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("远程调用异常");
        }
        return new UsernameAndPasswordDTO(username, password);
    }

}
