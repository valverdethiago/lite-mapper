package com.valverdethiago.pocs.litemapper.converters;

public interface RuntimeConverter<S, T> {
    T convert(S source, Class<T> targetClass);
}
