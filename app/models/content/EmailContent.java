package models.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type",value = "EMAIL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailContent extends BaseContent implements Content{

    @NotNull
    private String text;
    @NotNull
    private String subject;
    @Email(message = "Email should be valid")
    private String email;

    @Override
    public @NotNull Type getType() {
        return Type.EMAIL;
    }
}
