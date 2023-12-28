package com.example.api.gateway.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomMapper extends ModelMapper {
    public <T, D> List<D> convertList(List<T> sourceList, Class<D> targetClass) {
        return sourceList
                .stream()
                .map(sourceElement-> map(sourceElement,targetClass))
                .toList();
    }
}
