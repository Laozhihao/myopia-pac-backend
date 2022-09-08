package com.wupol.myopia.business.core.hospital.service;

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

}
