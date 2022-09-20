package com.wupol.myopia.business.api.school.management.domain.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 0-6检查记录
 *
 * @author hang.yuan 2022/9/16 18:39
 */
@Data
@Accessors(chain = true)
public class PreschoolCheckRecordVO implements Serializable {


    /**
     * 记录
     */
    private IPage<PreschoolCheckRecordDTO> pageData;


}
