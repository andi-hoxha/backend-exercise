package mongo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MongoDateConverter extends JsonSerializer<Date> {
    private SimpleDateFormat formatter  = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(value == null){
            gen.writeNull();
        }
        gen.writeString(formatter.format(value));
    }
}
