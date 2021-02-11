package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.goprime.mongolay.annotations.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mongo.serializers.MongoDateConverter;
import mongo.serializers.ObjectIdDeSerializer;
import mongo.serializers.ObjectIdStringSerializer;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(collection = "Dashboard")
public class Dashboard extends BaseModel{
    @NotEmpty(message = "Dashboard Name cannot be empty")
    private String name;
    @NotEmpty(message = "Dashboard description cannot be empty")
    private String description;
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    private ObjectId parentId;

    @JsonSerialize(using = MongoDateConverter.class)
    private Date createdAt;

    @BsonIgnore
    List<Dashboard> children;

}
