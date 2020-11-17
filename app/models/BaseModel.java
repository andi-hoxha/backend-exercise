package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import mongo.serializers.ObjectIdDeSerializer;
import mongo.serializers.ObjectIdStringSerializer;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import types.RecordStatus;

import java.io.Serializable;


@Data
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseModel implements Cloneable, Serializable {
    @BsonId
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    private ObjectId id;

    @JsonIgnore
    private RecordStatus recordStatus = RecordStatus.ACTIVE;

    public void setId(ObjectId id) {
        if (id == null) {
            this.id = null;
            return;
        }
        this.id = id;
    }


    @Override
    public BaseModel clone() throws CloneNotSupportedException {
        BaseModel clone = (BaseModel) super.clone();
        clone.setId(this.getId());
        clone.setRecordStatus(this.getRecordStatus());
        return clone;
    }

}
