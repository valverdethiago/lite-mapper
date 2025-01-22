package com.valverdethiago.pocs.litemapper;

import com.valverdethiago.pocs.litemapper.converters.ReflectionMapper;
import com.valverdethiago.pocs.litemapper.example.Destination;
import com.valverdethiago.pocs.litemapper.example.Source;
import com.valverdethiago.pocs.litemapper.mapstruct.MapStructMapper;
import org.mapstruct.factory.Mappers;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class MapperBenchmark {

    private final ReflectionMapper reflectionMapper = new ReflectionMapper();
    private final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    private Source source;

    @Setup
    public void setup() {
        source = new Source();
        source.setName("John Doe");
        source.setAge("30");
    }

    @Benchmark
    public Destination benchmarkReflectionMapper() {
        return (Destination) reflectionMapper.convert(source, Destination.class);
    }

    @Benchmark
    public Destination benchmarkMapStruct() {
        return mapStructMapper.map(source);
    }
}
