package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel{
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotEmpty
    private String parentId;
    @NotEmpty
    private Timestamp createdAt;

    private List<String> readACL;
    private List<String> writeACL;
}
