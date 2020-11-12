package modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.typesafe.config.Config;
import mongo.serializers.SerializationAttributes;
import play.libs.Json;

public class ObjectMapperProvider implements Provider<ObjectMapper> {

    @Inject
    Config config;

    @Override
    public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();
// SET ENCRYPTION ATTRIBUTES
        DeserializationConfig deserializationConfig = mapper.getDeserializationConfig()
                .withAttribute(SerializationAttributes.PRIVATE_KEY.toString(), config.getString("encryption.private_key"))
                .withAttribute(SerializationAttributes.ENCRYPTION_TYPE.toString(), config.getString("encryption.type"));
        mapper.setConfig(deserializationConfig);
        SerializationConfig serializationConfig = mapper.getSerializationConfig()
                .withAttribute(SerializationAttributes.PUBLIC_KEY.toString(), config.getString("encryption.public_key"))
                .withAttribute(SerializationAttributes.ENCRYPTION_TYPE.toString(), config.getString("encryption.type"));
        mapper.setConfig(serializationConfig);
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
// Needs to set to Json helper
        Json.setObjectMapper(mapper);
        return mapper;
    }
}
