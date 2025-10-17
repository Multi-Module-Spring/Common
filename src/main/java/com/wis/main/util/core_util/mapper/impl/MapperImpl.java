package com.wis.main.util.core_util.mapper.impl;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wis.main.util.core_util.mapper.JsonUtils;
import com.wis.main.util.core_util.mapper.LocalDateTimeWithoutZoneDeserializer;
import com.wis.main.util.core_util.mapper.Mapper;
import com.wis.main.util.core_util.mapper.MappingStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class MapperImpl implements Mapper {
    public final static ObjectMapper jsonMapper;
    public final static ObjectMapper fieldMapper;

    static {
        jsonMapper = new ObjectMapper();
        setup(jsonMapper);
        fieldMapper = JsonMapper.builder().disable(MapperFeature.USE_ANNOTATIONS).build();
        setup(fieldMapper);
    }

    private ObjectMapper mapper(MappingStrategy strategy) {
        return switch (strategy) {
            case MappingStrategy.JSON_PROPERTY -> jsonMapper;
            case null, default -> fieldMapper;
        };
    }

    public static void setup(ObjectMapper objectMapper) {
        JsonUtils.setup(objectMapper);

        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeWithoutZoneDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public <F, T> T mapViaString(F from, Class<T> pojoType) {
        return mapViaString(from, pojoType, null);
    }

    @Override
    public <F, T> T mapViaString(F from, Class<T> pojoType, MappingStrategy strategy) {
        try {
            byte[] json = mapper(strategy).writeValueAsBytes(from);
            return mapper(strategy).readValue(json, pojoType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mapAsString object", e);
        }
    }

    @Override
    public <T> T deepClone(T object, Class<T> clazz) {
        return deepClone(object, clazz, null);
    }

    @Override
    public <T> T deepClone(T object, Class<T> clazz, MappingStrategy strategy) {
        try {
            byte[] json = mapper(strategy).writeValueAsBytes(object);
            return mapper(strategy).readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep clone object", e);
        }
    }

    @Override
    public <T> T mapTo(Map<String, Object> mapData, Class<T> pojoType) {
        return mapTo(mapData, pojoType, null);
    }

    @Override
    public <T> T mapTo(Map<String, Object> mapData, Class<T> pojoType, MappingStrategy strategy) {
        return mapper(strategy).convertValue(mapData, pojoType);
    }

    @Override
    public <F, T> T mapTo(F from, Class<T> type) {
        return mapTo(from, type, null);
    }

    @Override
    public <F, T> T mapTo(F from, Class<T> type, MappingStrategy strategy) {
        if (type == String.class) {
            try {
                return type.cast(mapper(strategy).writeValueAsString(from));
            } catch (Exception e) {
                return type.cast(from.toString());
            }
        }
        return mapper(strategy).convertValue(from, type);
    }

    @Override
    public <F, T> Function<F, T> mapTo(Class<T> type) {
        return mapTo(type, (MappingStrategy) null);
    }

    @Override
    public <F, T> Function<F, T> mapTo(Class<T> type, MappingStrategy strategy) {
        return from -> mapper(strategy).convertValue(from, type);
    }

    @Override
    public <F, T> T updateValue(T to, F from) {
        try {
            return fieldMapper.updateValue(to, from);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <F, T> T updateValue(T to, F from, MappingStrategy strategy) {
        try {
            return mapper(strategy).updateValue(to, from);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void switchValue(T left, T right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Objects to switch cannot be null");
        }

        if (left instanceof List<?> l && right instanceof List<?> r) {
            if (l.size() != r.size()) {
                throw new IllegalArgumentException("Cannot switch lists with different sizes");
            }
            for (int i = 0; i < l.size(); i++) {
                Object a = l.get(i);
                Object b = r.get(i);

                Object tmpA = deepClone(a, (Class<Object>) a.getClass());
                updateValue(a, b);
                updateValue(b, tmpA);
            }
        } else {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) left.getClass();
            T tmp = deepClone(left, clazz);
            updateValue(left, right);
            updateValue(right, tmp);
        }
    }

    @Override
    public <T> T read(String json, Class<T> clazz) {
        try {
            return jsonMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON", e);
        }
    }

    @Override
    public String write(Object object) {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON", e);
        }
    }

}
