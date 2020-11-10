package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import mongo.serializers.ObjectIdDeSerializer;
import mongo.serializers.ObjectIdStringSerializer;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import types.RecordStatus;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseModel implements Cloneable, Serializable {
    @BsonId
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    private ObjectId id;

    @Setter(AccessLevel.NONE)
    @BsonIgnore
    @JsonIgnore
    protected Date createdAt;
    @BsonIgnore
    @JsonIgnore
    protected Date updatedAt;
    @JsonIgnore
    private RecordStatus recordStatus = RecordStatus.ACTIVE;

    public void setId(ObjectId id) {
        if (id == null) {
            this.id = null;
            this.createdAt = null;
            return;
        }
        this.id = id;
        this.createdAt = new Date(id.getTimestamp());
    }

    public Date getLastUpdate() {
        if (updatedAt != null) {
            return updatedAt;
        }
        return createdAt;
    }

    @Override
    public BaseModel clone() throws CloneNotSupportedException {
        BaseModel clone = (BaseModel) super.clone();
        clone.setId(this.getId());
        clone.setUpdatedAt(this.getUpdatedAt());
        clone.setRecordStatus(this.getRecordStatus());
        return clone;
    }

}
