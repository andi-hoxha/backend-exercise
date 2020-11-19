package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.util.*;

public class ConverterUtil {

    public static <T> List<T> jsonNodeToList(JsonNode jsonNode, Class<T> objectClass){
        List<T> list = new ArrayList<>();
        for(JsonNode node: jsonNode){
            list.add(Json.fromJson(node,objectClass));
        }
        return list;
    }
}
