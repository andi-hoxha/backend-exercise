package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.UniqueElements;
import play.data.validation.Constraints;

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
    @NotEmpty
    String email;
    List<Role> roles;
}
