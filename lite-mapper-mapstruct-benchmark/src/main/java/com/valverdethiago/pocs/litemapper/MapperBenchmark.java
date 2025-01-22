package com.valverdethiago.pocs.litemapper;

import com.valverdethiago.pocs.litemapper.converters.ReflectionMapper;
import com.valverdethiago.pocs.litemapper.example.Destination;
import com.valverdethiago.pocs.litemapper.example.Source;
import com.valverdethiago.pocs.litemapper.mapstruct.MapStructMapper;
import org.mapstruct.factory.Mappers;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // Default mode: Average time per operation
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Default time unit
@State(Scope.Thread) // Each thread gets its own state instance
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS) // Warm-up for JIT optimizations
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // Measurement phase
@Fork(1) // Number of JVM forks
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
