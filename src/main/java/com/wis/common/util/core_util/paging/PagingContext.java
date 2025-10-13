package com.wis.common.util.core_util.paging;

import com.wis.common.model.core.PagingInfo;

public interface PagingContext {
    void set(PagingInfo paging);
    PagingInfo get();
    void clear();
    void setTotalCount(Integer totalCount);
    Integer getTotalCount();
    Integer getTotalPages();
}

