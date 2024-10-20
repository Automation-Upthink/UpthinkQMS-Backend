package gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.google.gson.GsonBuilder;

public class CustomGsonBuilder {

    private static final JsonParser jsonParser;
    private static Map<String, WF_RuntimeTypeAdapterFactory> typeAdapterFactories;
    private static Gson defaultGson;
    private static Gson prettyGson;

    static {
        jsonParser = new JsonParser();
        CustomGsonBuilder.typeAdapterFactories = new HashMap<String, WF_RuntimeTypeAdapterFactory>();
        CustomGsonBuilder.defaultGson = createGson(false);
        CustomGsonBuilder.prettyGson = createGson(true);
    }

    private static Gson createGson(final boolean prettyPrint){
        final GsonBuilder registerTypeFactory = new GsonBuilder()
                .registerTypeAdapter((Type)Date.class, (Object)new DateTypeAdapter())
                .registerTypeAdapter((Type)java.sql.Date.class, (Object)new DateTypeAdapter())
                .registerTypeAdapter((Type)GsonObject.class, (Object)new GsonObjectAdapter())
                .registerTypeAdapter((Type)GsonArray.class, (Object)new GsonArrayAdapter())
                .registerTypeAdapter((Type)Class.class, (Object)new ClassAdapter());

        final Iterator<WF_RuntimeTypeAdapterFactory> iterator = CustomGsonBuilder.typeAdapterFactories.values().iterator();
        while(iterator.hasNext()){
            registerTypeFactory.registerTypeAdapterFactory((TypeAdapterFactory) iterator.next());
        }
        if(prettyPrint){
            registerTypeFactory.setPrettyPrinting();
        }
        return registerTypeFactory.create();
    }

    public static String prettyPrint(final GsonDTO gsonDTO) {
        return CustomGsonBuilder.prettyGson.toJson((Object)gsonDTO);
    }

    static JsonElement parseJson(final String jsonString) throws CustomGsonException {
        try{
            return CustomGsonBuilder.jsonParser.parse(jsonString);
        } catch(Exception e){
            throw new CustomGsonException(e);
        }
    }

    public static String serializeToJsonString(final Object obj) {
        return CustomGsonBuilder.defaultGson.toJson(obj);
    }

    public static GsonObject serializeToJsonObject(final Object obj) {
        return new GsonObject(CustomGsonBuilder.defaultGson.toJsonTree(obj).getAsJsonObject());
    }

    static <T> T deserializeFromGsonObject(final GsonObject gsonObject, final Class<T> classOfT) {
        if(gsonObject == null) {
            return null;
        }
        return (T)CustomGsonBuilder.defaultGson.fromJson((JsonElement)gsonObject.jsonObject, (Class)classOfT);
    }

    static <T> T deserializeFromJsonString(final String s, Class<T> classOfT) {
        return (T)CustomGsonBuilder.defaultGson.fromJson(s, (Class)classOfT);
    }




    private static final class DateTypeAdapter implements JsonDeserializer<Date>, JsonSerializer<Date>{

        @Override
        public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }
    }

    private static final class GsonObjectAdapter implements JsonDeserializer<GsonObject>, JsonSerializer<GsonObject> {

        @Override
        public GsonObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public JsonElement serialize(GsonObject gsonObject, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }
    }

    private static final class GsonArrayAdapter implements JsonDeserializer<GsonArray>, JsonSerializer<GsonArray>{

        @Override
        public GsonArray deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public JsonElement serialize(GsonArray gsonArray, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }
    }
    private static final class ClassAdapter implements JsonDeserializer<Class<?>>, JsonSerializer<Class<?>>{

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if(jsonElement.isJsonNull()) {
                return null;
            }
            try{
                return Class.forName(jsonElement.getAsString());
            }catch(final ClassNotFoundException e){
                return null;
            }
        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }
    }
}
