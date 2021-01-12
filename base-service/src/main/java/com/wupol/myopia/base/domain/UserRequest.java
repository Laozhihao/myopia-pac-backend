package com.wupol.myopia.base.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户请求体
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class UserRequest {

    private Integer orgId;

    private List<String> idCards;

    public UserRequest(Integer orgId, List<String> idCards) {
        this.orgId = orgId;
        this.idCards = idCards;
    }
}
