package actions;

import models.BaseModel;
import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@With(Accessibilty.class)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessControl {
    String key();
    String collectionName();
    Class<? extends BaseModel> value();

}
