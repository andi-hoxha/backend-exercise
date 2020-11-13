package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel{
    private String name;
    private String description;
    private String parentId;
    private Timestamp createdAt;
    private List<String> readACL;
    private List<String> writeACL;
}
