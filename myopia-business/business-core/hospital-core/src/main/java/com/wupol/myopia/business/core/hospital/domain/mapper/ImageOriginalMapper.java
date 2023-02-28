package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.hospital.domain.model.ImageOriginal;
import org.apache.ibatis.annotations.Param;

/**
 * 图像原始表Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-09-08
 */
public interface ImageOriginalMapper extends BaseMapper<ImageOriginal> {

    ImageOriginal getLastImage(@Param("patientId") Integer patientId, @Param("hospitalId") Integer hospitalId);

}
