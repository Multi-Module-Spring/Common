package com.wis.executation;

import com.wis.configuration.Payload;

public interface ActionService<REQUEST, MODEL> {
    MODEL execute(Payload payload, REQUEST request);
}
