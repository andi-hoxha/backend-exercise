package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseModel {
    @NotEmpty
    String username;
    @NotEmpty
    String password;
    @Email(message = "Email should be valid")
    String email;

    @BsonIgnore
    @JsonIgnore
    @JsonProperty("roleIds")
    List<String> roleIds;

    List<Role> roles;

}
