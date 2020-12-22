package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.HospitalListRequest;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Service
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    /**
     * 保存医院
     *
     * @param hospital 医院实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveHospital(Hospital hospital) {
        generateAccountAndPassword();
        hospital.setHospitalNo(generateHospitalNo());
        return baseMapper.insert(hospital);
    }

    /**
     * 获取医院列表
     *
     * @param request   请求入参
     * @param govDeptId 部门id
     * @return Page<Hospital> {@link com.baomidou.mybatisplus.core.metadata.IPage}
     */
    public Page<Hospital> getHospitalList(HospitalListRequest request, Integer govDeptId) {

        Page<Hospital> page = new Page<>(request.getPage(), request.getLimit());
        QueryWrapper<Hospital> hospitalWrapper = new QueryWrapper<>();

        hospitalWrapper.in("gov_dept_id", getAllDeptId(govDeptId));
        hospitalWrapper.ne("status", Const.IS_DELETED);

        if (StringUtils.isNotBlank(request.getName())) {
            hospitalWrapper.like("name", request.getName());
        }
        if (null != request.getType()) {
            hospitalWrapper.eq("type", request.getType());
        }
        if (null != request.getKind()) {
            hospitalWrapper.eq("kind", request.getKind());
        }
        if (null != request.getLevel()) {
            hospitalWrapper.eq("level", request.getLevel());
        }
        if (null != request.getCode()) {
            hospitalWrapper.like("city_code", request.getCode())
                    .or()
                    .like("area_code", request.getCode());
        }
        return baseMapper.selectPage(page, hospitalWrapper);
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
    private Long generateHospitalNo() {
        return 123L;
    }

    /**
     * 获取下级所有部门
     *
     * @param id 部门id
     * @return List<Integer>
     */
    public List<Integer> getAllDeptId(Integer id) {
        return Lists.newArrayList(id);
    }

}
