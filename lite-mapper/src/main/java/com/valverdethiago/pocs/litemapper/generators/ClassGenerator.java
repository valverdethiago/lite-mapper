package com.valverdethiago.pocs.litemapper.generators;

import com.valverdethiago.pocs.litemapper.converters.RuntimeConverter;

public interface ClassGenerator {
    <S, D> RuntimeConverter<S, D> generateMapperClass(Class<S> sourceClass, Class<D> destinationClass);
}
