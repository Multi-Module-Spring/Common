package com.wis.configuration;

import com.wis.util.core_util.mapper.impl.MapperImpl;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true)
                .setSourceNameTransformer((name, type) -> {
                    String[] parts = name.split("_");
                    StringBuilder builder = new StringBuilder(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        builder.append(Character.toUpperCase(parts[i].charAt(0)))
                                .append(parts[i].substring(1));
                    }
                    return builder.toString();
                });
        mapper.addConverter(new AbstractConverter<Timestamp, LocalDateTime>() {
            @Override
            protected LocalDateTime convert(Timestamp source) {
                return source == null ? null : source.toLocalDateTime();
            }
        });
        return mapper;
    }

    @Bean
    public MapperImpl mapper(ModelMapper modelMapper) {
        return new MapperImpl(modelMapper);
    }
}
