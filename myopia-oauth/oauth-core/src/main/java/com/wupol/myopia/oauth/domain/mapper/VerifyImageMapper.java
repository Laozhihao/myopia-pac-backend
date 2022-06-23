package com.wupol.myopia.oauth.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.oauth.domain.model.VerifyImage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 验证图片Mapper接口
 * @author hang.yuan 2022/4/29 09:30
 */
public interface VerifyImageMapper extends BaseMapper<VerifyImage> {


    void batchSaveCompress(@Param("verifyImageList") List<String> verifyImageList);

    VerifyImage findById(@Param("id") Integer id);

}
