package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.hospital.domain.mapper.ImageOriginalMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ImageOriginal;
import org.springframework.stereotype.Service;

/**
 * @author Simple4H
 */
@Service
public class ImageOriginalService extends BaseService<ImageOriginalMapper, ImageOriginal> {

    /**
     * 通过md5获取
     *
     * @param md5 md5
     *
     * @return ImageOriginal
     */
    public ImageOriginal getByMd5(String md5) {
        return findOne(new ImageOriginal().setMd5(md5));
    }

    /**
     * 获取当天最后一条数据
     *
     * @param patientId  患者Id
     * @param hospitalId 医院Id
     *
     * @return ImageOriginal
     */
    public ImageOriginal getLastImage(Integer patientId, Integer hospitalId) {
        return baseMapper.getLastImage(patientId, hospitalId);
    }

}
