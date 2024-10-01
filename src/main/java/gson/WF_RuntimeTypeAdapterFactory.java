package gson;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


public class WF_RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory{

    private final Class<?> baseType;  // base class for which the factory can create adapters;
    private final String  typeFieldName;   // This field stores the name of the JSON field that determines the type of the object
    private final Map<String, Class<?>> stringToSubclassMapper; // maps string to subclass
    private final Map<Class<?>, String> subclassToStringMapper; // maps subclass type to string
    private final boolean maintainType; // indicates whether the type field should be maintained during serialization and deserialization

    private WF_RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName, boolean maintainType) {
        this.subclassToStringMapper = new LinkedHashMap<Class<?>, String>();
        this.stringToSubclassMapper = new LinkedHashMap<String, Class<?>>();
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
        this.maintainType = maintainType;
    }

    public static <T> WF_RuntimeTypeAdapterFactory<T> of(final Class<T> baseType, final String typeFieldName, final boolean maintainType) {
        return new WF_RuntimeTypeAdapterFactory<T>(baseType, typeFieldName, maintainType);
    }

    public static <T> WF_RuntimeTypeAdapterFactory<T> of(final Class<T> baseType, final String typeFieldName) {
        return new WF_RuntimeTypeAdapterFactory<T>(baseType, typeFieldName, false);
    }

    public static <T> WF_RuntimeTypeAdapterFactory<T> of(final Class<T> baseType) {
        return new WF_RuntimeTypeAdapterFactory<T>(baseType, "type", false);
    }

    public final WF_RuntimeTypeAdapterFactory<T> registerSubtype(final Class<? extends T> type){
        return this.registerSubtype(type, type.getSimpleName());
    }

    public final WF_RuntimeTypeAdapterFactory<T> registerSubtype(final Class<? extends T> type, final String label){
        if (type == null || label == null){
            throw new NullPointerException();
        }
        if(this.subclassToStringMapper.containsKey(type) || this.stringToSubclassMapper.containsKey(label)){
            throw new IllegalArgumentException("types and labels must be unique");
        }
        this.subclassToStringMapper.put(type, label);
        this.stringToSubclassMapper.put(label, type);
        return this;
    }


    @Override
    public <Type> TypeAdapter<Type> create(Gson gson, TypeToken<Type> typeToken) {
        if(typeToken.getRawType() != this.baseType){
            return null;
        }
        final LinkedHashMap linkedHashMap1 = new LinkedHashMap();
        final LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        for (final Map.Entry entry : this.stringToSubclassMapper.entrySet()) {
            final TypeAdapter delegateAdapter = gson.getDelegateAdapter((TypeAdapterFactory)this, TypeToken.get((Class)entry.getValue()));
            linkedHashMap1.put(entry.getValue(), delegateAdapter); // Class is the first entry
            linkedHashMap2.put(entry.getKey(), delegateAdapter);   // String is the first entry
        }
        return (TypeAdapter<Type>) new TypeAdapter<Type>() {
            // This reads the input character stream into a JsonElement
            @Override
            public final Type read(final JsonReader inputCharacterStream) throws IOException {
                final JsonElement parsedJsonElement = Streams.parse(inputCharacterStream);
                JsonElement jsonElement;
                // If we want to maintain the type of the subclass
                if(WF_RuntimeTypeAdapterFactory.this.maintainType){
                    jsonElement = parsedJsonElement.getAsJsonObject().get(WF_RuntimeTypeAdapterFactory.this.typeFieldName);
                } else {
                    jsonElement = parsedJsonElement.getAsJsonObject().remove(WF_RuntimeTypeAdapterFactory.this.typeFieldName);
                }
                if (jsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + WF_RuntimeTypeAdapterFactory.this.baseType + " because it does not define a field named " + WF_RuntimeTypeAdapterFactory.this.typeFieldName);
                }
                final String asString = jsonElement.getAsString();
                final TypeAdapter typeAdapter;
                if ((typeAdapter = (TypeAdapter) linkedHashMap2.get(asString)) == null) {
                    throw new JsonParseException("cannot deserialize " + WF_RuntimeTypeAdapterFactory.this.baseType + " subtype named " + asString + "; did you forget to register a subtype?");
                }
                return (Type)typeAdapter.fromJsonTree(parsedJsonElement);

            }


            // This method writes Json Object
            @Override
            public void write(JsonWriter outputCharacterStream, Type type) throws IOException {
                final Class<?> class1 = type.getClass();
                final String s = WF_RuntimeTypeAdapterFactory.this.subclassToStringMapper.get(class1);
                final TypeAdapter typeAdapter;
                if((typeAdapter = (TypeAdapter) linkedHashMap1.get(class1)) == null) {
                    throw new JsonParseException("cannot serialize " + class1.getName() + "; did you forget to register a subtype?");
                }
                // Convert to json object
                final JsonObject asJsonObject = typeAdapter.toJsonTree((Object) type).getAsJsonObject();
                if(WF_RuntimeTypeAdapterFactory.this.maintainType) {
                    Streams.write((JsonElement) asJsonObject, outputCharacterStream);
                    return;
                }
                final JsonObject jsonObject = new JsonObject();
                // If already the adapter type is assigned to the field name
                if(asJsonObject.has(WF_RuntimeTypeAdapterFactory.this.typeFieldName)) {
                    throw new JsonParseException("cannot serialize " + class1.getName() + " because it already defines a field named " + WF_RuntimeTypeAdapterFactory.this.typeFieldName);
                }
                jsonObject.add(WF_RuntimeTypeAdapterFactory.this.typeFieldName, (JsonElement) new JsonPrimitive(s));
                for (final Map.Entry entry : asJsonObject.entrySet()) {
                    jsonObject.add((String)entry.getKey(), (JsonElement) entry.getValue());
                }
                Streams.write((JsonElement) jsonObject, outputCharacterStream);
            }
        }.nullSafe();
    };
}



















