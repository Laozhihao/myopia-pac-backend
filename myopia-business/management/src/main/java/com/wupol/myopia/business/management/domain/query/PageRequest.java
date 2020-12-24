package com.wupol.myopia.business.management.domain.query;

import lombok.Getter;
import lombok.Setter;

/**
 * 分页基类
 *
 * @author Simple4H
 */
@Setter
@Getter
public class PageRequest {

    private Integer current;

    private Integer size;


}
