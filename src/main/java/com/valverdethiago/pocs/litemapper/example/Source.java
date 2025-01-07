package com.valverdethiago.pocs.litemapper.example;

import com.valverdethiago.pocs.litemapper.annotations.MapTo;
import lombok.Data;

@Data
public class Source {
    @MapTo(targetField = "destinationName")
    private String name;

    @MapTo(targetField = "age")
    private String age;

    // Getters and Setters
}
