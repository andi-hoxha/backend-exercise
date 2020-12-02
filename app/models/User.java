package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mongo.serializers.PasswordDeserializer;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import types.UserRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"password"},allowSetters = true)
public class User extends BaseModel {
    @NotEmpty
    private String username;
    @JsonDeserialize(using = PasswordDeserializer.class)
    @NotEmpty
    private String password;
    @Email(message = "Email should be valid")
    private String email;

    @BsonIgnore
    @JsonIgnore
    @JsonProperty("roleIds")
    private List<String> roleIds;

    private List<Role> roles;

    private String accessToken = UserRole.EMPTY.name();

}
