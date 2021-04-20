package com.wupol.myopia.business.common.utils.interfaces;

import java.util.List;

/**
 *
 * @author Alix
 * @date 2021/03/30
 */
public interface HasCreatorNameLikeAndCreateUserIds<T> {
    String getCreatorNameLike();
    T setCreateUserIds(List<Integer> creatorIds);
}
