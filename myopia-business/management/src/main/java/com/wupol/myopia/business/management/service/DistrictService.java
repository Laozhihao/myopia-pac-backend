package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.mapper.DistrictMapper;
import com.wupol.myopia.business.management.domain.model.District;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class DistrictService extends BaseService<DistrictMapper, District> {

    /**
     * 生成业务ID
     * @param type 类型 {@link Const.MANAGEMENT_TYPE}
     * @return 业务ID
     */
    public String generateSn(Integer type) {
        // TODO: 实现业务逻辑
        switch (type) {
            case 1:
                return "1L";
            case 2:
                return "2L";
            case 3:
                return "3L";
            case 4:
                return "4L";
            case 5:
                return "5L";
        }
        return null;
    }
}
