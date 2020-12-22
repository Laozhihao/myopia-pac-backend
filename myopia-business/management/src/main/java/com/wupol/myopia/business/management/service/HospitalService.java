package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.HospitalConst;
import com.wupol.myopia.business.management.domain.dto.HospitalListRequest;
import com.wupol.myopia.business.management.domain.mapper.HospitalMapper;
import com.wupol.myopia.business.management.domain.model.Hospital;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author HaoHao
 * @Date 2020-12-21
 */
@Service
public class HospitalService extends BaseService<HospitalMapper, Hospital> {

    @Transactional(rollbackFor = Exception.class)
    public Integer saveHospital(Hospital hospital) {
        generateAccountAndPassword(hospital);
        hospital.setHospitalNo(generateHospitalNo());
        return baseMapper.insert(hospital);
    }

    public Page<Hospital> getHospitalList(HospitalListRequest request) {

        Page<Hospital> page = new Page<>(request.getPage(), request.getLimit());
        QueryWrapper<Hospital> hospitalWrapper = new QueryWrapper<>();
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
        hospitalWrapper.ne("status", HospitalConst.IS_DELETED);
        return baseMapper.selectPage(page, hospitalWrapper);
    }

    private void generateAccountAndPassword(Hospital hospital) {

    }

    private Long generateHospitalNo() {
        return 123L;
    }

}
