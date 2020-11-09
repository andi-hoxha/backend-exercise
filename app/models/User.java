package models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {
    private String username;
    private String password;
    private List<Role> roles;
}
