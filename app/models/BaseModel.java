package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseModel {
    @BsonId
    private ObjectId id;

    @Setter(AccessLevel.NONE)
    protected Date createdAt;
    protected Date updatedAt;

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
        return clone;
    }

}
