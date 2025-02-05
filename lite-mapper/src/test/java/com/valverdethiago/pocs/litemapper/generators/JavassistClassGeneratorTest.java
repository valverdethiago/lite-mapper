package com.valverdethiago.pocs.litemapper.generators;

import com.valverdethiago.pocs.litemapper.converters.RuntimeConverter;
import com.valverdethiago.pocs.litemapper.example.Destination;
import com.valverdethiago.pocs.litemapper.example.Source;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavassistClassGeneratorTest {

    private static Source source;
    private static RuntimeConverter<Source, Destination> converter;
    private static ClassGenerator generator;

    @BeforeAll
    public static void setup() {
        source = new Source();
        source.setName("John Doe");
        source.setAge("30");
        generator = new JavassistClassGenerator();
        converter = generator.generateMapperClass(Source.class, Destination.class);
    }

    @Test
    void testGenerateMapperClass() {
        assertNotNull(converter, "Generated class should not be null");
        assertTrue(converter.getClass().getName().contains("SourceToDestinationMapper"),
                "Generated class name should follow convention");
    }

    @Test
    void testConversion() {
        Destination destination = converter.convert(source, Destination.class);

        assertEquals(source.getName(), destination.getDestinationName());
        assertEquals(Integer.parseInt(source.getAge()), destination.getAge());
    }
}
