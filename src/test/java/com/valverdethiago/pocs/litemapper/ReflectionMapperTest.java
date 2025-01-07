package com.valverdethiago.pocs.litemapper;

import com.valverdethiago.pocs.litemapper.converters.ReflectionMapper;
import com.valverdethiago.pocs.litemapper.example.Source;
import com.valverdethiago.pocs.litemapper.example.Destination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReflectionMapperTest {

    @Test
    public void testMapping() {
        Source source = new Source();
        source.setName("John Doe");
        source.setAge("30");

        ReflectionMapper mapper = new ReflectionMapper();
        Destination destination = (Destination) mapper.convert(source, Destination.class);

        assertEquals("John Doe", destination.getDestinationName());
        assertEquals(30, destination.getAge());
    }
}
