package com.wis.main.executation;

import com.wis.main.configuration.Payload;

public interface ActionService<REQUEST, MODEL> {
    MODEL execute(Payload payload, REQUEST request);
}
