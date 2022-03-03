package com.wupol.myopia.business.core.school.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.vo.SchoolGradeClassVO;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学校Service
 *
 * @author HaoHao
 */
@Service
@Log4j2
public class SchoolService extends BaseService<SchoolMapper, School> {

    @Resource
    private SchoolAdminService schoolAdminService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private DistrictService districtService;

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveSchool(School school) {
        Assert.hasLength(school.getSchoolNo(), "学校编号不能为空");
        Assert.notNull(school.getDistrictId(), "行政区域ID不能为空");
        if (checkSchoolName(school.getName(), null)) {
            throw new BusinessException("学校名称重复，请确认");
        }
        District district = districtService.getById(school.getDistrictId());
        Assert.notNull(district, "无效行政区域");
        school.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));

        baseMapper.insert(school);
        // oauth系统中增加学校状态信息
        oauthServiceClient.addOrganization(new Organization(school.getId(), SystemCode.SCHOOL_CLIENT,
                UserType.OTHER, school.getStatus()));
        initGradeAndClass(school.getId(), school.getType(), school.getCreateUserId());
        return generateAccountAndPassword(school, StringUtils.EMPTY);
    }

    /**
     * 新增学校
     *
     * @param schoolDTO 学校扩展实体
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveSchoolDTO(SchoolDTO schoolDTO) {
        Assert.hasLength(schoolDTO.getSchoolNo(), "学校编号不能为空");
        Assert.notNull(schoolDTO.getDistrictId(), "行政区域ID不能为空");
        if (checkSchoolName(schoolDTO.getName(), null)) {
            throw new BusinessException("学校名称重复，请确认");
        }
        District district = districtService.getById(schoolDTO.getDistrictId());
        Assert.notNull(district, "无效行政区域");
        schoolDTO.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        baseMapper.insert(schoolDTO);

        List<SchoolGradeDTO> schoolGradeDTOS = schoolDTO.getGradeDTOs();

        /**
         * TODO:由于要获取年级ID，左右在for循环中进行批量操作，时候再优化
         */
        for (SchoolGradeDTO schoolGradeDTO:schoolGradeDTOS){
            SchoolGrade schoolGrade = new SchoolGrade();
            BeanUtil.copyProperties(schoolGradeDTO,schoolGrade);
            schoolGradeService.saveOrUpdate(schoolGrade);

            List<String> names = Arrays.asList(schoolGradeDTO.getClassNames().split(","));

            List<SchoolClass> schoolClassesTemp = new ArrayList<>();
            for (String name:names){
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setSchoolId(schoolDTO.getId());
                schoolClass.setGradeId(schoolGrade.getId());
                schoolClass.setName(name);
                schoolClass.setCreateUserId(schoolDTO.getCreateUserId());
                schoolClassesTemp.add(schoolClass);
            }
            schoolClassService.saveOrUpdateBatch(schoolClassesTemp);
        }

        // oauth系统中增加学校状态信息
        oauthServiceClient.addOrganization(new Organization(schoolDTO.getId(), SystemCode.SCHOOL_CLIENT,
                UserType.OTHER, schoolDTO.getStatus()));
        initGradeAndClass(schoolDTO.getId(), schoolDTO.getType(), schoolDTO.getCreateUserId());
        return generateAccountAndPassword(schoolDTO, StringUtils.EMPTY);
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
        school.setStatus(CommonConst.STATUS_IS_DELETED);
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
        List<SchoolAdmin> schoolAdminList = schoolAdminService.findByList(new SchoolAdmin().setSchoolId(request.getId()));
        if (CollectionUtils.isEmpty(schoolAdminList)) {
            log.error("更新学校状态异常，找不到学校管理员。学校ID:{}", request.getId());
            throw new BusinessException("数据异常!");
        }
        // 更新用户状态
        UserDTO user = new UserDTO();
        user.setUserIds(Lists.newArrayList(request.getUserId()));
        user.setStatus(request.getStatus());
        oauthServiceClient.updateUserStatusBatch(user);
        // 更新学校状态
        baseMapper.updateStatus(request);
        return 1;
    }

    /**
     * 更新学校管理员用户状态
     *
     * @param request 用户信息
     * @return boolean
     **/
    public boolean updateSchoolAdminUserStatus(StatusRequest request) {
        SchoolAdmin schoolAdmin = schoolAdminService.findOne(new SchoolAdmin().setSchoolId(request.getId()).setUserId(request.getUserId()));
        Assert.notNull(schoolAdmin, "不存在该用户");
        UserDTO user = new UserDTO();
        user.setId(request.getUserId());
        user.setStatus(request.getStatus());
        oauthServiceClient.updateUser(user);
        return true;
    }

    /**
     * 获取学校的筛查记录详情
     *
     * @param schoolIds 筛查记录详情ID
     * @return 详情
     */
    public List<School> getSchoolByIdsAndName(List<Integer> schoolIds, String schoolName) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<School> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(School::getId, schoolIds);
        if (StringUtils.isNotBlank(schoolName)) {
            queryWrapper.like(School::getName, schoolName);
        }
        return baseMapper.selectList(queryWrapper);
    }


    /**
     * 重置密码
     *
     * @param request 请求参数
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(ResetPasswordRequest request) {
        SchoolAdmin schoolAdmin = schoolAdminService.findOne(new SchoolAdmin().setSchoolId(request.getId()).setUserId(request.getUserId()));
        if (Objects.isNull(schoolAdmin)) {
            throw new BusinessException("该账号不存");
        }
        return resetOAuthPassword(request.getUsername(), schoolAdmin.getUserId());
    }

    /**
     * 生成账号密码
     *
     * @param school 学校信息
     * @param name   子账号名称
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO generateAccountAndPassword(School school, String name) {
        // 账号规则：jsfkx + 序号
        String password = PasswordAndUsernameGenerator.getSchoolAdminPwd();
        String username;
        if (StringUtils.isBlank(name)) {
            username = PasswordAndUsernameGenerator.getSchoolAdminUserName(schoolAdminService.count() + 1);
        } else {
            username = name;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(school.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(school.getName())
                .setCreateUserId(school.getCreateUserId())
                .setSystemCode(SystemCode.SCHOOL_CLIENT.getCode());
        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        schoolAdminService.insertStaff(school.getId(), school.getCreateUserId(), school.getGovDeptId(), user.getId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 重置密码
     *
     * @param username 用户名
     * @param userId   OAuth2 的userId
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(String username, Integer userId) {
        String password = PasswordAndUsernameGenerator.getSchoolAdminPwd();
        oauthServiceClient.resetPwd(userId, password);
        return new UsernameAndPasswordDTO(username, password);
    }


    /**
     * 获取学校的筛查记录详情
     *
     * @param schoolIds 筛查记录详情ID
     * @return 详情
     */
    public List<School> getSchoolByIds(List<Integer> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        return baseMapper.selectBatchIds(schoolIds);
    }

    /**
     * 模糊查询所有学校名称
     *
     * @param query 查询条件
     * @return List<School>
     */
    public List<School> getBy(SchoolQueryDTO query) {
        return baseMapper.getByQuery(query);
    }

    /**
     * 通过名字获取学校
     *
     * @param name 名字
     * @return List<School>
     */
    public List<School> getBySchoolName(String name) {
        return baseMapper.getByName(name);
    }

    /**
     * 学校编号是否被使用
     *
     * @param schoolId 学校ID
     * @param schoolNo 学校编号
     * @return Boolean.TRUE-使用 Boolean.FALSE-没有使用
     */
    public Boolean checkSchoolNo(Integer schoolId, String schoolNo) {
        return baseMapper.getByNoNeId(schoolNo, schoolId).size() > 0;
    }

    /**
     * 通过学校编号获取学校
     *
     * @param schoolNo 学校编号
     * @return School
     */
    public School getBySchoolNo(String schoolNo) {
        return baseMapper.getBySchoolNo(schoolNo);
    }

    /**
     * 批量通过学校编号获取学校
     *
     * @param schoolNos 学校编号
     * @return School
     */
    public List<School> getBySchoolNos(List<String> schoolNos) {
        return baseMapper.getBySchoolNos(schoolNos);
    }

    /**
     * 通过districtId获取学校
     *
     * @param districtId 行政区域ID
     * @return List<School>
     */
    public List<School> getByDistrictId(Integer districtId) {
        return baseMapper.getByDistrictId(districtId);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<School> getByPage(Page<?> page, SchoolQueryDTO query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 批量通过id获取
     *
     * @param ids 学校id
     * @return List<School>
     */
    public List<School> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 检查学校名称是否重复
     *
     * @param schoolName 学校名称
     * @param id         学校ID
     * @return 是否重复
     */
    public boolean checkSchoolName(String schoolName, Integer id) {
        return !baseMapper.getByNameNeId(schoolName, id).isEmpty();
    }

    /**
     * 初始化学校年级和班级信息
     *
     * @param schoolId     学校ID
     * @param type         学校类型
     * @param createUserId 创建人
     */
    private void initGradeAndClass(Integer schoolId, Integer type, Integer createUserId) {
        List<SchoolGrade> schoolGrades = new ArrayList<>();
        switch (type) {
            case 0:
                // 小学
                schoolGrades = initGrade(SchoolAge.PRIMARY.code, schoolId, createUserId);
                break;
            case 1:
                // 初级中学
                schoolGrades = initGrade(SchoolAge.JUNIOR.code, schoolId, createUserId);
                break;
            case 2:
                // 高级中学
                schoolGrades = initGrade(SchoolAge.HIGH.code, schoolId, createUserId);
                break;
            case 3:
                // 完全中学
                schoolGrades = Lists.newArrayList(Iterables.concat(
                        initGrade(SchoolAge.JUNIOR.code, schoolId, createUserId),
                        initGrade(SchoolAge.HIGH.code, schoolId, createUserId)));
                break;
            case 4:
                // 九年一贯制学校
                schoolGrades = Lists.newArrayList(Iterables.concat(
                        initGrade(SchoolAge.PRIMARY.code, schoolId, createUserId),
                        initGrade(SchoolAge.JUNIOR.code, schoolId, createUserId)));
                break;
            case 5:
            case 7:
                // 其他
                // 十二年一贯制学校
                schoolGrades = Lists.newArrayList(Iterables.concat(
                        initGrade(SchoolAge.PRIMARY.code, schoolId, createUserId),
                        initGrade(SchoolAge.JUNIOR.code, schoolId, createUserId),
                        initGrade(SchoolAge.HIGH.code, schoolId, createUserId)));
                break;
            case 6:
                // 职业高中
                schoolGrades = initGrade(SchoolAge.VOCATIONAL_HIGH.code, schoolId, createUserId);
                break;
            case 8:
                // 幼儿园
                schoolGrades = initGrade(SchoolAge.KINDERGARTEN.code, schoolId, createUserId);
                break;
            default:
                break;
        }
        if (!CollectionUtils.isEmpty(schoolGrades) && schoolGradeService.saveBatch(schoolGrades)) {
            // 批量新增班级
            List<Integer> schoolGradeIds = schoolGrades.stream().map(SchoolGrade::getId).collect(Collectors.toList());
            batchCreateClass(createUserId, schoolId, schoolGradeIds);
        }
    }

    /**
     * 根据类型初始化班级信息
     *
     * @param type         类型 {@link SchoolAge}
     * @param schoolId     学校ID
     * @param createUserId 创建人
     * @return List<SchoolGrade>
     */
    private List<SchoolGrade> initGrade(Integer type, Integer schoolId, Integer createUserId) {
        return GradeCodeEnum.gradeByMap.get(type).stream()
                .map(s -> new SchoolGrade(createUserId, schoolId, s.getCode(), s.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 批量新增班级信息
     *
     * @param createUserId 创建人
     * @param schoolId     学校ID
     * @param gradeIds     年级ids
     */
    private void batchCreateClass(Integer createUserId, Integer schoolId, List<Integer> gradeIds) {

        gradeIds.forEach(g -> {
            ArrayList<SchoolClass> schoolClasses = Lists.newArrayList(
                    new SchoolClass(g, createUserId, schoolId, "1班", 35),
                    new SchoolClass(g, createUserId, schoolId, "2班", 35),
                    new SchoolClass(g, createUserId, schoolId, "3班", 35));
            schoolClassService.saveBatch(schoolClasses);
        });
    }

    /**
     * 更新OAuh2 username
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public void updateOAuthName(Integer userId, String username) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId)
                .setUsername(username);
        oauthServiceClient.updateUser(userDTO);
    }

    public Set<Integer> getAllSchoolDistrictIdsBySchoolIds(Set<Integer> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptySet();
        }
        return baseMapper.selectDistrictIdsBySchoolIds(schoolIds);
    }

    /**
     * 通过条件获取学习列表
     *
     * @param pageRequest      分页请求
     * @param schoolQueryDTO   条件
     * @param districtId 行政区域id
     * @param userIds          用户ID
     * @param districtCode     行政区域-省Code
     * @return IPage<SchoolResponseDTO>
     */
    public IPage<SchoolResponseDTO> getSchoolListByCondition(PageRequest pageRequest, SchoolQueryDTO schoolQueryDTO,
                                                             Integer districtId,
                                                             List<Integer> userIds, Integer districtCode) {
        return baseMapper.getSchoolListByCondition(pageRequest.toPage(), schoolQueryDTO, districtId, districtCode, userIds);
    }

    public String getNameById(Integer id) {
        School school = getById(id);
        return Objects.nonNull(school) ? school.getName() : "";
    }

    public School getBySchoolId(Integer id) {
        return baseMapper.getBySchoolId(id);
    }

    /**
     * 获取状态未更新的学校（已到合作开始时间未启用，已到合作结束时间未停止）
     *
     * @return
     */
    public List<School> getUnhandleSchool(Date date) {
        return baseMapper.getByCooperationTimeAndStatus(date);
    }

    /**
     * CAS更新机构状态，当且仅当源状态为sourceStatus，且限定id
     *
     * @param id
     * @param targetStatus
     * @param sourceStatus
     * @return
     */
    @Transactional
    public int updateSchoolStatus(Integer id, Integer targetStatus, Integer sourceStatus) {
        // 更新机构状态成功
        int result = baseMapper.updateSchoolStatus(id, targetStatus, sourceStatus);
        if (result > 0) {
            // 更新oauth上机构的状态
            oauthServiceClient.updateOrganization(new Organization(id, SystemCode.SCHOOL_CLIENT, UserType.OTHER, targetStatus));
        }
        return result;
    }

    /**
     * 获取指定合作结束时间的学校信息
     *
     * @param start 开始时间早于该时间才处理
     * @param end   指定结束时间，精确到天
     * @return
     */
    public List<School> getByCooperationEndTime(Date start, Date end) {
        return baseMapper.getByCooperationEndTime(start, end);
    }

    /**
     * 获取学校信息
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @return SchoolGradeClassVO
     */
    public SchoolGradeClassVO getBySchoolIdAndGradeIdAndClassId(Integer schoolId, Integer gradeId, Integer classId) {
        return baseMapper.getBySchoolIdAndGradeIdAndClassId(schoolId, gradeId, classId);
    }

    /**
     * 检验学校合作信息是否合法
     *
     * @param school
     */
    public void checkSchoolCooperation(School school) {
        if (!school.checkCooperation()) {
            throw new BusinessException("合作信息非法，请确认");
        }
    }

    /**
     * 更新结果通知
     *
     * @param id                 学校Id
     * @param resultNoticeConfig 结果通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateResultNoticeConfig(Integer id, ResultNoticeConfig resultNoticeConfig) {
        School school = getBySchoolId(id);
        if (Objects.isNull(school)) {
            throw new BusinessException("学校数据异常");
        }
        school.setResultNoticeConfig(resultNoticeConfig);
        updateById(school);
    }
}