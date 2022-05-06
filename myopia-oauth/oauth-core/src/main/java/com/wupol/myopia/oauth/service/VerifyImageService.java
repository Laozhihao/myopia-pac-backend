package com.wupol.myopia.oauth.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.VerifyImageMapper;
import com.wupol.myopia.oauth.domain.model.VerifyImage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 验证图片Service
 *
 * @author hang.yuan 2022/4/29 09:32
 */
@Service
public class VerifyImageService extends BaseService<VerifyImageMapper, VerifyImage> {


    public void batchSaveCompress(List<String> verifyImageList ){
        baseMapper.batchSaveCompress(verifyImageList);
    }

    public VerifyImage findById(Integer id){
        return baseMapper.findById(id);
    }

}
