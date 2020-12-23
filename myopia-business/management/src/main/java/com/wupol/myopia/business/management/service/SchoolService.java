package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.SchoolListRequest;
import com.wupol.myopia.business.management.domain.mapper.SchoolMapper;
import com.wupol.myopia.business.management.domain.model.School;
import org.apache.commons.lang3.StringUtils;
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
    private HospitalService hospitalService;

    /**
     * 新增学校
     *
     * @param school 学校实体
     * @return 新增个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveSchool(School school) {
        generateAccountAndPassword();
        school.setSchoolNo(generateSchoolNo());
        return baseMapper.insert(school);
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
     * @param request   请求体
     * @param govDeptId 部门ID
     * @return Page<School> {@link Page}
     */
    public Page<School> getSchoolList(SchoolListRequest request, Integer govDeptId) {

        Page<School> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<School> schoolWrapper = new QueryWrapper<>();

        schoolWrapper.in("gov_dept_id", hospitalService.getAllByDeptId(govDeptId));
        schoolWrapper.ne("status", Const.STATUS_IS_DELETED);

        if (null != request.getSchoolNo()) {
            schoolWrapper.like("school_no", request.getSchoolNo());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            schoolWrapper.like("name", request.getName());
        }
        if (null != request.getType()) {
            schoolWrapper.like("type", request.getType());
        }
        if (null != request.getCode()) {
            schoolWrapper.like("city_code", request.getCode())
                    .or()
                    .like("area_code", request.getCode());
        }
        return baseMapper.selectPage(page, schoolWrapper);

    }


    /**
     * 生成账号密码
     */
    private void generateAccountAndPassword() {

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
