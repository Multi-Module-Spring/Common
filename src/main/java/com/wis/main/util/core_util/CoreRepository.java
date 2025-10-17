package com.wis.main.util.core_util;

import com.wis.main.util.core_util.database.DBPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ApplicationScope
public abstract class CoreRepository extends CoreBean {

    @Autowired
    protected DBPool dbPool;

    public CoreRepository() {
        resetParams();
    }

    private static final ThreadLocal<List<Object>> threadLocalParams =
            ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<Map<String, Object>> threadLocalMapParams =
            ThreadLocal.withInitial(HashMap::new);

    public List<Object> getParams() {
        return threadLocalParams.get();
    }

    public Map<String, Object> getMapParams() {
        return threadLocalMapParams.get();
    }

    public void resetParams() {
        threadLocalParams.remove();
        threadLocalMapParams.remove();

        threadLocalParams.set(new ArrayList<>());
        threadLocalMapParams.set(new HashMap<>());

        this.params = threadLocalParams.get();
        this.mapParams = threadLocalMapParams.get();
    }


    public List<Object> params = getParams();
    public Map<String, Object> mapParams = getMapParams();
}


