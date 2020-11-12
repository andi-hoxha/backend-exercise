package services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.RequestException;
import play.libs.Files;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class SerializationService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    ObjectMapper objectMapper;

    public <T> CompletableFuture<JsonNode> toJsonNode(T result){
        return CompletableFuture.supplyAsync(()->{
            try {
                return Json.toJson(result);
            }catch (Exception e){
                e.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"exception while parsing object"));
            }
        }, ec.current());
    }

    public <T> CompletableFuture<T> parseBodyOfType(Http.Request request, Class<T> objectClass){
        return CompletableFuture.supplyAsync(()-> this.syncParseBodyOfType(request,objectClass),ec.current());
    }

    public <T> T syncParseBodyOfType(Http.Request request, Class<T> objectClass){
        try{
            Optional<T> body = request.body().parseJson(objectClass);
            if(!body.isPresent()){
                throw new RequestException(Http.Status.BAD_REQUEST,"exception while parsing body");
            }
            return body.get();
        }catch (RequestException ex){
            ex.printStackTrace();
            throw new CompletionException(ex);
        }catch (Exception e){
            e.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"unknown exception, please check your service"));
        }
    }


    public JsonNode fileToObjectNode(File file) throws IOException{
        JsonParser parser = objectMapper.getFactory().createParser(file);
        JsonNode node = objectMapper.readTree(parser);
        parser.close();
        return node;
    }

    public <T> CompletableFuture<List<T>> parseFileOfType(Http.Request request, String key, Class<T> objectClass){
        return CompletableFuture.supplyAsync(()->{
            Http.MultipartFormData<Files.TemporaryFile> data = request.body().asMultipartFormData();
            Http.MultipartFormData.FilePart<Files.TemporaryFile> filePart = data.getFile("key");
            if(data.getFiles().size() == 0){
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"invalid parameters"));
            }
            try{
                Files.TemporaryFile file = filePart.getRef();
                File javaFile  = file.copyTo(new File("public/files/dummys.json")).toFile();
                JsonNode content = this.fileToObjectNode(javaFile);
                return parseJsonListOfType(content,objectClass);
            }catch (JsonProcessingException e){
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"exception while parsing body"));
            }catch (IOException e){
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"File parsing exception"));
            }
            catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"uknown exception, service is unavailable"));
            }
        },ec.current());
    }

    public static <T> List<T> parseJsonListOfType (JsonNode json, Class<T> type) {
        try {
            if (!json.isArray()) {
                throw new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters");
            }
            List<T> list = new ArrayList<>();
            for (JsonNode node: json) {
                list.add(Json.fromJson(node, type));
            }
            return list;
        } catch (RequestException | ClassCastException ex) {
            ex.printStackTrace();
            throw new CompletionException(ex);
        } catch (CompletionException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "service_unavailable"));
        }
    }

    public <T> CompletableFuture<List<T>> parseListBodyOfType(Http.Request request,Class<T> objectClass){
        return CompletableFuture.supplyAsync(()-> this.syncParseListBodyOfType(request,objectClass),ec.current());
    }

    private <T> List<T> syncParseListBodyOfType(Http.Request request, Class<T> objectClass) {
        return parseJsonListOfType(request.body().asJson(),objectClass);
    }
}
