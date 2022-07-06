package com.jmunoz.hoaxify.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import java.io.IOException;

// También se podría haber usado la anotación @Service, como se usó en la clase UserService
// Pero puesto que vamos a configurar el comportamiento de la app (Jackson en concreto), dejamos @Configuration
@Configuration
public class SerializationConfiguration {

    // Module es un objeto Jackson y Spring lo usará automáticamente
    @Bean
    public Module springDataPageModule() {
        JsonSerializer<Page> pageSerializer = new JsonSerializer<Page>() {

            // Aquí recibimos el objeto Page y con el generator reescribiremos los campos que queremos ver
            // en el response
            @Override
            public void serialize(Page value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
                generator.writeStartObject();
                // campos de Page. Los pasamos todos en la respuesta
                generator.writeNumberField("numberOfElements", value.getNumberOfElements());
                generator.writeNumberField("totalElements", value.getTotalElements());
                generator.writeNumberField("totalPages", value.getTotalPages());
                generator.writeNumberField("number", value.getNumber());
                generator.writeNumberField("size", value.getSize());
                generator.writeBooleanField("first", value.isFirst());
                generator.writeBooleanField("last", value.isLast());
                generator.writeBooleanField("next", value.hasNext());
                generator.writeBooleanField("previous", value.hasPrevious());

                // Esta es la lista de usuarios. Ya configuramos JsonViews para serializarlos así que lo usamos
                generator.writeFieldName("content");
                serializers.defaultSerializeValue(value.getContent(), generator);

                generator.writeEndObject();
            }
        };

        // Primer parámetro: tipo de objeto
        // Segundo parámetro: el pageSerializer que hemos creado
        return new SimpleModule().addSerializer(Page.class, pageSerializer);
    }
}
