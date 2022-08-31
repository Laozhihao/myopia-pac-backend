package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QesFieldMappingMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QesFieldMapping;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QesFieldMappingService extends BaseService<QesFieldMappingMapper, QesFieldMapping> {

    /**
     * 根据QES管理ID查询qes字段映射集合
     * @param qesId QES管理ID
     */
    public List<QesFieldMapping> listByQesId(Integer qesId){
        LambdaQueryWrapper<QesFieldMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QesFieldMapping::getQesId,qesId);
        return baseMapper.selectList(queryWrapper);
    }
}
