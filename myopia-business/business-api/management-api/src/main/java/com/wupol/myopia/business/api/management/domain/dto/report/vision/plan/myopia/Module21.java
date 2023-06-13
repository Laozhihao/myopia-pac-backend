package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.IGradeTable;
import com.wupol.myopia.business.api.management.util.RadioAndCountUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.List;

/**
 * 小学及以上教育阶段各年级学生近视分布情况
 *
 * @author Simple4H
 */
@Data
public class Module21 {

    /**
     * 表格
     */
    private List<Object> table;

    /**
     * 表格
     */
    @Data
    public static class TableItem implements IGradeTable {

        /**
         * 描述
         */
        private String desc;

        /**
         * 近视人数
         */
        private Long myopiaCount;

        /**
         * 低度近视人数
         */
        private Long lightMyopiaCount;

        /**
         * 低度近视占比
         */
        private String lightMyopiaProportion;

        /**
         * 高度近视人数
         */
        private Long highMyopiaCount;

        /**
         * 高度近视占比
         */
        private String highMyopiaProportion;

        @Override
        public Object getGradeTable(String desc, List<StatConclusion> statConclusion) {
            TableItem tableItem = new TableItem();
            tableItem.setDesc(desc);

            Long myopiaCount = RadioAndCountUtil.getMyopiaRadioAndCount(statConclusion).getCount();
            tableItem.setMyopiaCount(myopiaCount);

            Long lightCount = RadioAndCountUtil.getLightMyopiaRadioAndCount(statConclusion).getCount();
            tableItem.setLightMyopiaCount(lightCount);
            tableItem.setLightMyopiaProportion(BigDecimalUtil.divide(lightCount, myopiaCount));

            Long highCount = RadioAndCountUtil.getHighMyopiaRadioAndCount(statConclusion).getCount();
            tableItem.setHighMyopiaCount(highCount);
            tableItem.setHighMyopiaProportion(BigDecimalUtil.divide(highCount, myopiaCount));
            return tableItem;
        }
    }
}
