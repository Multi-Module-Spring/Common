package com.wis.util.core_util;

import com.wis.util.core_util.database.DBPool;
import com.wis.util.core_util.paging.PagingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public abstract class CoreRepository extends CoreBean {

    @Autowired
    protected DBPool dbPool;

    @Autowired
    protected PagingContext pagingContext;

    //Map<String, Object>
    private final ThreadLocal<Map<String, Object>> threadLocalValues =
            ThreadLocal.withInitial(HashMap::new);


    protected void beginValues() {
        threadLocalValues.get().clear();
    }


    protected Map<String, Object> values() {
        return threadLocalValues.get();
    }


    protected void endValues() {
        beginValues();
    }
    //List<Object>
    private final ThreadLocal<List<Object>> threadLocalValuesListObject =
            ThreadLocal.withInitial(ArrayList::new);


    protected void beginValuesNoSQLBuilder() {
        threadLocalValuesListObject.get().clear();
    }


    protected List<Object> valuesN() {
        return threadLocalValuesListObject.get();
    }


    protected void endValuesNoSQLBuilder() {
        beginValuesNoSQLBuilder();
    }


}
