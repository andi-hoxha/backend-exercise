package models.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;
import models.BaseModel;
import mongo.serializers.ObjectIdDeSerializer;
import mongo.serializers.ObjectIdStringSerializer;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Data
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailContent.class,name = "EMAIL"),
        @JsonSubTypes.Type(value = TextContent.class,name = "TEXT"),
        @JsonSubTypes.Type(value = ImageContent.class,name = "IMAGE"),
        @JsonSubTypes.Type(value = LineContent.class,name = "LINE")

})
@BsonDiscriminator(key = "type",value = "NONE")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseContent extends BaseModel implements Content {

    @BsonId
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    private ObjectId dashboardId;

    @NotNull
    private Type type = Type.NONE;

    private List<String> readACL = Collections.emptyList();
    private List<String> writeACL = Collections.emptyList();
}
