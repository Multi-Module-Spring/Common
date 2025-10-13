package com.wis.common.executation;

import com.wis.common.configuration.Payload;

public interface ActionService<REQUEST, MODEL> {
    MODEL execute(Payload payload, REQUEST request);
}
