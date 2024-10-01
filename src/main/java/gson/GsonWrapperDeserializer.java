package gson;

import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializer;

public abstract class GsonWrapperDeserializer<T> implements JsonDeserializer<T>
{
    public final T deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }
        return this.fromGson(new GsonObject(jsonElement.getAsJsonObject()));
    }

    protected abstract T fromGson(final GsonObject p0) throws JsonParseException;
}