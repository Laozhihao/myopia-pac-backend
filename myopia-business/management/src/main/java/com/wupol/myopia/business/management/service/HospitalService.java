package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.HospitalListRequest;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Service
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    @Resource
    private HospitalStaffService hospitalStaffService;

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveHospital(Hospital hospital) {
        hospital.setHospitalNo(generateHospitalNo());
        baseMapper.insert(hospital);
        return generateAccountAndPassword(hospital.getCreateUserId(), hospital.getId());
    }

    /**
     * 更新医院信息
     *
     * @param hospital 医院实体类
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateHospital(Hospital hospital) {
        return baseMapper.updateById(hospital);
    }

    /**
     * 删除医院
     *
     * @param id           医院id
     * @param createUserId 创建用户
     * @param govDeptId    部门id
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedHospital(Integer id, Integer createUserId, Integer govDeptId) {
        Hospital hospital = new Hospital();
        // TODO: 获取登陆用户id, 部门id
        hospital.setId(id);
        hospital.setCreateUserId(Const.CREATE_USER_ID);
        hospital.setGovDeptId(Const.GOV_DEPT_ID);
        hospital.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(hospital);
    }

    /**
     * 获取医院列表
     *
     * @param query   请求入参
     * @param govDeptId 部门id
     * @return Page<Hospital> {@link Page}
     */
    public Page<Hospital> getHospitalList(HospitalQuery query, Integer govDeptId) {

        QueryWrapper<Hospital> hospitalWrapper = new QueryWrapper<>();

        InQueryAppend(hospitalWrapper, "gov_dept_id", getAllByDeptId(govDeptId));
        notEqualsQueryAppend(hospitalWrapper, "status", Const.STATUS_IS_DELETED);

        if (StringUtils.isNotBlank(query.getName())) {
            likeQueryAppend(hospitalWrapper, "name", query.getName());
        }
        if (null != query.getType()) {
            equalsQueryAppend(hospitalWrapper, "type", query.getType());
        }
        if (null != query.getKind()) {
            equalsQueryAppend(hospitalWrapper, "kind", query.getKind());
        }
        if (null != query.getLevel()) {
            equalsQueryAppend(hospitalWrapper, "level", query.getLevel());
        }
        if (null != query.getCode()) {
            orLikeQueryAppend(hospitalWrapper,
                    Lists.newArrayList("city_code", "area_code"),
                    query.getCode());
        }
        return baseMapper.selectPage(query.getQueryPage(), hospitalWrapper);
    }

    /**
     * 生成账号密码
     */
    private Integer generateAccountAndPassword(Integer createUserId, Integer hospitalId) {
        // TODO: 创建对应的staff
        return hospitalStaffService.saveStaff(createUserId, hospitalId, Const.STAFF_USER_ID);
    }

    /**
     * 生成编号
     *
     * @return Long
     */
    private Long generateHospitalNo() {
        return 123L;
    }

    /**
     * 获取下级所有部门
     *
     * @param id 部门id
     * @return List<Integer>
     */
    public List<Integer> getAllByDeptId(Integer id) {
        return Lists.newArrayList(id);
    }

}
