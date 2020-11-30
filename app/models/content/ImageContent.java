package models.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@BsonDiscriminator(key = "type",value = "IMAGE")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageContent extends BaseContent implements Content{

    @NotEmpty
    private String url;

    @Override
    public @NotNull Type getType() {
        return Type.IMAGE;
    }
}
