package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.wupol.myopia.business.core.hospital.domain.model.ImageDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图像详情表Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-09-08
 */
public interface ImageDetailMapper extends BaseMapper<ImageDetail> {

    List<ImageDetail> getTodayPatientFundusFile(@Param("patientId") Integer patientId, @Param("hospitalId") Integer hospitalId);

}
