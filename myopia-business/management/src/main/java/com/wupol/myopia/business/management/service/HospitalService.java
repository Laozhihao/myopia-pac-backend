package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Service
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    @Resource
    private HospitalStaffService hospitalStaffService;

    @Resource
    private HospitalMapper hospitalMapper;

    @Resource
    private GovDeptService govDeptService;

    @Resource
    private DistrictService districtService;

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveHospital(Hospital hospital) {
        hospital.setHospitalNo(districtService.generateSn(Const.MANAGEMENT_TYPE.HOSPITAL));
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
     * @param pageRequest 分页
     * @param query       请求入参
     * @param govDeptId   部门id
     * @return IPage<Hospital> {@link IPage}
     */
    public IPage<Hospital> getHospitalList(PageRequest pageRequest, HospitalQuery query, Integer govDeptId) {
        return hospitalMapper.getHospitalListByCondition(pageRequest.toPage(), govDeptService.getAllSubordinate(govDeptId),
                query.getName(), query.getType(), query.getKind(), query.getLevel(), query.getCode());
    }

    /**
     * 生成账号密码
     */
    private Integer generateAccountAndPassword(Integer createUserId, Integer hospitalId) {
        // TODO: 创建对应的staff
        return hospitalStaffService.saveStaff(createUserId, hospitalId, Const.STAFF_USER_ID);
    }
}
