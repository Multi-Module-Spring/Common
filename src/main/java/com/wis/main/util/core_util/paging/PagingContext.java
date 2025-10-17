package com.wis.main.util.core_util.paging;

import com.wis.main.model.core.PagingInfo;

public interface PagingContext {
    void set(PagingInfo paging);
    PagingInfo get();
    void clear();
    void setTotalCount(Integer totalCount);
    Integer getTotalCount();
    Integer getTotalPages();
}

