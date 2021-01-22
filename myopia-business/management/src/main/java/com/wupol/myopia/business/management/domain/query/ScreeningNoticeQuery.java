package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningNoticeQuery extends ScreeningNotice {
    /**
     * 名称
     */
    private String nameLike;
    /**
     * 创建人
     */
    private String createUser;
}
