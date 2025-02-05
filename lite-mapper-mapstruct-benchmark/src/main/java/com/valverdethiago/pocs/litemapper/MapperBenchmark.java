package com.valverdethiago.pocs.litemapper;

import com.valverdethiago.pocs.litemapper.converters.CachedReflectionConverter;
import com.valverdethiago.pocs.litemapper.converters.CustomMapper;
import com.valverdethiago.pocs.litemapper.converters.ReflectionConverter;
import com.valverdethiago.pocs.litemapper.converters.RuntimeConverter;
import com.valverdethiago.pocs.litemapper.example.Destination;
import com.valverdethiago.pocs.litemapper.example.Source;
import com.valverdethiago.pocs.litemapper.generators.JavassistClassGenerator;
import com.valverdethiago.pocs.litemapper.mapstruct.MapStructMapper;
import org.mapstruct.factory.Mappers;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // Default mode: Average time per operation
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Default time unit
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.MILLISECONDS) // Warm-up for JIT optimizations
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS) // Measurement phase
@Fork(1) // Number of JVM forks
public class MapperBenchmark {

    private final ReflectionConverter<Source, Destination> reflectionConverter = new ReflectionConverter<>();
    private final CachedReflectionConverter<Source, Destination> cachedReflectionConverter = new CachedReflectionConverter<>();
    private final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);
    private final RuntimeConverter<Source, Destination> javassistConverter = new JavassistClassGenerator()
            .generateMapperClass(Source.class, Destination.class);

    private Source source;

    @Setup
    public void setup() {
        source = new Source();
        source.setName("John Doe");
        source.setAge("30");
    }

    @Benchmark
    public Destination benchmarkCachedReflectionMapper() {
        return cachedReflectionConverter.convert(source, Destination.class);
    }

    @Benchmark
    public Destination benchmarkReflectionMapper() {
        return reflectionConverter.convert(source, Destination.class);
    }

    @Benchmark
    public Destination benchmarkMapStruct() {
        return mapStructMapper.map(source);
    }

    @Benchmark
    public Destination benchmarkJavassist() {
        return javassistConverter.convert(source, Destination.class);
    }
}
