package models.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotNull;

@BsonDiscriminator(key = "type",value = "TEXT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextContent extends BaseContent implements Content {

    @NotNull
    private String text;

    @Override
    public @NotNull Type getType() {
        return Type.TEXT;
    }
}
