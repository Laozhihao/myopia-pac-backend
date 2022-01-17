package com.wupol.myopia.base.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 家庭信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class FamilyInfoVO {

    /**
     * 家庭成员
     */
    private List<MemberInfo> member;


    @Getter
    @Setter
    public static class MemberInfo {

        /**
         * 名称
         */
        private String name;

        /**
         * 联系方式
         */
        private String phone;

        /**
         * 生日
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date birthday;

        /**
         * 文凭
         */
        private String diploma;

        /**
         * 职业
         */
        private String profession;

    }

}
