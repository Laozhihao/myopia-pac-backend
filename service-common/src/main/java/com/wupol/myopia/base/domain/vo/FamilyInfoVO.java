package com.wupol.myopia.base.domain.vo;

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

    private List<MemberInfo> member;


    @Getter
    @Setter
    static class MemberInfo {

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
