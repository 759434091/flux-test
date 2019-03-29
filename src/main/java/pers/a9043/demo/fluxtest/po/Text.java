package pers.a9043.demo.fluxtest.po;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luxueneng
 * @since 2019-03-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Text {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    ObjectId id;
    String text;

    public static class ObjectIdSerializer extends JsonSerializer<ObjectId> {
        @Override
        public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeString(objectId.toString());
        }
    }
}
