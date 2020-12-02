package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mongo.serializers.MongoDateConverter;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard extends BaseModel{
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    private String parentId;

    @JsonSerialize(using = MongoDateConverter.class)
    private Date createdAt;

    private List<String> readACL = Collections.emptyList();
    private List<String> writeACL = Collections.emptyList();

    @BsonIgnore
    List<Dashboard> children;

}
