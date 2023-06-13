package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia;

import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.IGradeTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.ISchoolTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import com.wupol.myopia.business.api.management.util.RadioAndCountUtil;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.List;

/**
 * 各学校近视率情况
 *
 * @author Simple4H
 */
@Data
public class Module22 {

    /**
     * 表格
     */
    private List<Object> table;

    /**
     * 表格
     */
    @Data
    public static class TableItem implements ISchoolTable {

        /**
         * 描述
         */
        private String desc;

        /**
         * 学校人数
         */
        private String schoolType;

        /**
         * 有效人数
         */
        private Long validCount;

        /**
         * 近视人数
         */
        private Long myopiaCount;

        /**
         * 近视率
         */
        private String myopiaRadio;


        @Override
        public Object getSchoolTable(String desc, String schoolType, List<StatConclusion> statConclusion) {
            TableItem tableItem = new TableItem();
            tableItem.setDesc(desc);
            tableItem.setSchoolType(schoolType);
            tableItem.setValidCount((long) statConclusion.size());
            RadioAndCount myopia = RadioAndCountUtil.getMyopiaRadioAndCount(statConclusion);
            tableItem.setMyopiaCount(myopia.getCount());
            tableItem.setMyopiaRadio(myopia.getRadio());
            return tableItem;

        }
    }
}
