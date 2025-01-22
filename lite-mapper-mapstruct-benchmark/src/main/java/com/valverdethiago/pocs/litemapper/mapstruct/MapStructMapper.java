
package com.valverdethiago.pocs.litemapper.mapstruct;

    import com.valverdethiago.pocs.litemapper.example.Destination;
    import com.valverdethiago.pocs.litemapper.example.Source;
    import org.mapstruct.Mapper;
    import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface MapStructMapper {
    @Mapping(source = "name", target = "destinationName")
    @Mapping(source = "age", target = "age")
    Destination map(Source source);
}
