package com.wis.common.model.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PagingInfo {
    private Integer page;
    private Integer pageSize;

    public Integer getOffset() {
        return (page != null && pageSize != null && page > 0) ? (page - 1) * pageSize : null;
    }

    public Integer getLimit() {
        return pageSize;
    }
}

