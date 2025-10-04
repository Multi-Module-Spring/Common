package com.wis.util.core_util.paging.impl;

import com.wis.model.core.PagingInfo;
import com.wis.util.core_util.paging.PagingContext;
import org.springframework.stereotype.Component;

@Component
public class PagingContextImpl implements PagingContext {
    private final ThreadLocal<PagingInfo> pagingHolder = new ThreadLocal<>();
    private final ThreadLocal<Integer> totalCountHolder = new ThreadLocal<>();

    public void set(PagingInfo paging) {
        pagingHolder.set(paging);
    }

    public PagingInfo get() {
        return pagingHolder.get();
    }

    public void clear() {
        pagingHolder.remove();
        totalCountHolder.remove();
    }

    public void setTotalCount(Integer totalCount) {
        totalCountHolder.set(totalCount);
    }

    public Integer getTotalCount() {
        return totalCountHolder.get() != null ? totalCountHolder.get() : 0;
    }

    public Integer getTotalPages() {
        PagingInfo paging = pagingHolder.get();
        Integer totalCount = totalCountHolder.get();
        if (paging != null && totalCount != null && paging.getPageSize() > 0) {
            return (int) Math.ceil((double) totalCount / paging.getPageSize());
        }
        return 0;
    }
}

