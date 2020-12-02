package models.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mongo.serializers.PasswordDeserializer;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestModel {
    @NotNull
    private String username;
    @NotNull
    @JsonDeserialize(using = PasswordDeserializer.class)
    private String password;
}
