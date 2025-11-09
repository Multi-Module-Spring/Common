package com.wis.main.executation;

import com.wis.main.configuration.Payload;
import com.wis.main.util.core_util.CoreBean;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public abstract class CoreActionService<REQUEST, ACTION_MODEL, MODEL> extends CoreBean implements ActionService<REQUEST, MODEL> {

    @Override
    public MODEL execute(Payload payload, REQUEST request) {
        LocalDateTime now = dateTimeUtil.now();
        return execute(payload, now, request);
    }

    protected MODEL execute(Payload payload, LocalDateTime now, REQUEST request) {
        ACTION_MODEL actionModel = verify(payload, request, now);

        MODEL response = innerExecute(payload, actionModel, now);

        i18nResolver.resolveI18nFields(response);
        return response;
    }

    protected abstract ACTION_MODEL verify(Payload payload, REQUEST request, LocalDateTime now);

    protected abstract MODEL innerExecute(Payload payload, ACTION_MODEL actionModel, LocalDateTime now);
}
