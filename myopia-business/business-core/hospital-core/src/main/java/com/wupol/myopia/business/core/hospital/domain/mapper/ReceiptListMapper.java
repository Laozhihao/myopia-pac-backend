package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.hospital.domain.dto.ReceiptDTO;
import com.wupol.myopia.business.core.hospital.domain.model.ReceiptList;
import org.apache.ibatis.annotations.Param;


/**
 * 回执单Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
public interface ReceiptListMapper extends BaseMapper<ReceiptList> {

    /**
     * 获取回执单详情
     * @param id
     * @return
     */
    ReceiptDTO getDetails(@Param("id") Integer id);

}
