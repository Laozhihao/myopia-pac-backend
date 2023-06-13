package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.IGradeTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import com.wupol.myopia.business.api.management.util.RadioAndCountUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 小学及以上教育阶段各年级学生近视情况
 *
 * @author Simple4H
 */
@Data
public class Module2 {

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
         * 有效筛查人数
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

        /**
         * 男生人数
         */
        private Long maleCount;

        /**
         * 男生近视人数
         */
        private Long maleMyopiaCount;

        /**
         * 男生近视率
         */
        private String maleMyopiaRadio;

        /**
         * 女生人数
         */
        private Long femaleCount;

        /**
         * 女生近视人数
         */
        private Long femaleMyopiaCount;

        /**
         * 女生近视率
         */
        private String femaleMyopiaRadio;

        @Override
        public Object getGradeTable(String desc, List<StatConclusion> statConclusion) {
            Module2.TableItem tableItem = new Module2.TableItem();
            tableItem.setDesc(desc);
            tableItem.setValidCount((long) statConclusion.size());

            RadioAndCount myopiaRadioAndCount = RadioAndCountUtil.getMyopiaRadioAndCount(statConclusion);
            tableItem.setMyopiaCount(myopiaRadioAndCount.getCount());
            tableItem.setMyopiaRadio(myopiaRadioAndCount.getRadio());

            List<StatConclusion> maleStatConclusion = statConclusion.stream().filter(g -> Objects.equals(g.getGender(), GenderEnum.MALE.type)).collect(Collectors.toList());
            RadioAndCount maleMyopiaRadioAndCount = RadioAndCountUtil.getMyopiaRadioAndCount(maleStatConclusion);
            tableItem.setMaleCount((long) maleStatConclusion.size());
            tableItem.setMaleMyopiaCount(maleMyopiaRadioAndCount.getCount());
            tableItem.setMaleMyopiaRadio(maleMyopiaRadioAndCount.getRadio());

            List<StatConclusion> femaleStatConclusion = statConclusion.stream().filter(g -> Objects.equals(g.getGender(), GenderEnum.FEMALE.type)).collect(Collectors.toList());
            RadioAndCount femaleMyopiaRadioAndCount = RadioAndCountUtil.getMyopiaRadioAndCount(femaleStatConclusion);
            tableItem.setFemaleCount((long) femaleStatConclusion.size());
            tableItem.setFemaleMyopiaCount(femaleMyopiaRadioAndCount.getCount());
            tableItem.setFemaleMyopiaRadio(femaleMyopiaRadioAndCount.getRadio());
            return tableItem;
        }
    }
}
