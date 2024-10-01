package gson;

public class GsonDTO {

    public final GsonObject toGson(){
        return CustomGsonBuilder.serializeToJsonObject(this);
    }

    public final String toJson(){
        return CustomGsonBuilder.serializeToJsonString(this);
    }

    public boolean isError(){
        return false;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "--" + this.toJson();
    }

    public String toPrettyString(){
        return CustomGsonBuilder.prettyPrint(this);
    }

    public static <T extends GsonDTO> T fromGson(final GsonObject json, final String classOfTName) {
        try {
            return fromGson(json, Class.forName(classOfTName));
        }
        catch (final ClassNotFoundException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static <T extends  GsonDTO>T fromGson(final GsonObject jsonObject, final Class<?> classOfT){
        return (T) CustomGsonBuilder.deserializeFromGsonObject(jsonObject, classOfT);
    }

    public static <T extends GsonDTO>T fromJson(final String jsonString, final String classOfTName){
        try{
            return fromJson(jsonString, Class.forName(classOfTName));
        }catch(final ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public static <T extends GsonDTO>T fromJson(final String jsonString, final Class<?> classOfT){
        return (T)CustomGsonBuilder.deserializeFromJsonString(jsonString, classOfT);
    }
}
















