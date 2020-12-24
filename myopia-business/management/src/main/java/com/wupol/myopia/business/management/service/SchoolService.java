package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
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

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return 新增个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveSchool(School school) {
        school.setSchoolNo(generateSchoolNo());
        baseMapper.insert(school);
        return generateAccountAndPassword(school.getId(), school.getCreateUserId(), school.getGovDeptId());
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
     * @return IPage<School> {@link IPage}
     */
    public IPage<School> getSchoolList(PageRequest pageRequest, SchoolQuery schoolQuery, Integer govDeptId) {
        return schoolMapper.getSchoolListByCondition(pageRequest.toPage(),
                govDeptService.getAllSubordinate(govDeptId), schoolQuery.getName(),
                schoolQuery.getSchoolNo(), schoolQuery.getType(), schoolQuery.getCode());
    }


    /**
     * 生成账号密码
     */
    private Integer generateAccountAndPassword(Integer schoolId, Integer createUserId, Integer govDeptId) {
        // TODO: 生成账号密码，userId
        return SchoolStaffService.insertStaff(schoolId, createUserId, govDeptId, Const.CREATE_USER_ID);
    }

    /**
     * 生成编号
     *
     * @return Long
     */
    private Long generateSchoolNo() {
        return 123L;
    }
}
