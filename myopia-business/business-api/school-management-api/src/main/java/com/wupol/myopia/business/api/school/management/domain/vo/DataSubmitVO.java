package com.wupol.myopia.business.api.school.management.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 数据上传
 *
 * @author Simple4H
 */
@Getter
@Service
@NoArgsConstructor
public class DataSubmitVO {

    /**
     * 学号
     */
    private String sno;

    /**
     * 结果
     */
    private VisionScreeningResult result;

    public DataSubmitVO(String sno, VisionScreeningResult result) {
        this.sno = sno;
        this.result = result;
    }
}
