package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.goprime.mongolay.RelayModel;
import lombok.*;
import mongo.serializers.ObjectIdDeSerializer;
import mongo.serializers.ObjectIdStringSerializer;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import types.RecordStatus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Data
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseModel implements RelayModel,Cloneable, Serializable {
    @BsonId
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    private ObjectId id;

    protected Long updatedAt;

    protected Set<String> readACL = new HashSet<>();
    protected Set<String> writeACL = new HashSet<>();

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
        clone.setReadACL(new HashSet<>(this.getReadACL()));
        clone.setWriteACL(new HashSet<>(this.getWriteACL()));
        clone.setUpdatedAt(this.getUpdatedAt());
        return clone;
    }

}
