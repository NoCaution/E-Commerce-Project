package com.example.commonservice.Util;

import org.modelmapper.ModelMapper;
import java.util.List;

public class CustomMapper extends ModelMapper {
    public <T, D> List<D> convertList(List<T> sourceList, Class<D> targetClass) {
        return sourceList
                .stream()
                .map(sourceElement-> map(sourceElement,targetClass))
                .toList();
    }
}
