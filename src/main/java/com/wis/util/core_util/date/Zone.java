package com.wis.util.core_util.date;

import java.time.ZoneId;

public interface Zone {
    ZoneId GMT7 = ZoneId.of(TimeZone.GMT7);
    ZoneId UTC = ZoneId.of(TimeZone.UTC);
}
