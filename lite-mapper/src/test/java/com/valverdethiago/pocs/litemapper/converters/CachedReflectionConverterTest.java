package com.valverdethiago.pocs.litemapper.converters;

import com.valverdethiago.pocs.litemapper.example.Destination;
import com.valverdethiago.pocs.litemapper.example.Source;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachedReflectionConverterTest {

    @Test
    public void testMapping() {
        Source source = new Source();
        source.setName("John Doe");
        source.setAge("30");

        Destination destination = new CachedReflectionConverter<Source, Destination>()
                .convert(source, Destination.class);

        assertEquals("John Doe", destination.getDestinationName());
        assertEquals(30, destination.getAge());
    }
}
