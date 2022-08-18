package com.wupol.myopia.business.aggregation.export.excel.domain;

import com.wupol.myopia.business.common.utils.util.TwoTuple;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 生成rec数据业务实体
 *
 * @author hang.yuan 2022/8/17 11:14
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GenerateRecDataBO {

    private Integer schoolId;

    private TwoTuple<String,String> tuple;

    public GenerateRecDataBO(TwoTuple<String, String> tuple) {
        this.tuple = tuple;
    }
}
