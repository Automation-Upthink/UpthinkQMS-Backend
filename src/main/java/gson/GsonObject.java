package gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

public class GsonObject extends GsonDTO{

    JsonObject jsonObject;

    public GsonObject(){
        this(new JsonObject());
    }

    public GsonObject(final JsonObject jsonObject) {
        if(jsonObject == null){
            throw new IllegalArgumentException();
        }
        this.jsonObject = jsonObject;
    }

    public GsonObject(final String jsonString) throws CustomGsonException {
        this(CustomGsonBuilder.parseJson(jsonString).getAsJsonObject());
    }

    public GsonObject(Map<String, String> stringMap){
        this();
        if (stringMap != null){
            for(final Map.Entry entry: stringMap.entrySet()){
                this.jsonObject.addProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }
    }

    public final JsonObject getJsonObject(){
        return this.jsonObject;
    }

    public final GsonArray keyArray(){
        final GsonArray gsonArray = new GsonArray();
        final Iterator<String> iterator = this.keySet().iterator();
        while (iterator.hasNext()) {
            gsonArray.add(iterator.next());
        }
        return gsonArray;
    }

    public final Set keySet(){
        return this.jsonObject.keySet();
    }

    public final boolean has(final String key){
        return !this.isNull(key);
    }

    public final boolean isNull(final String key){
        return isNull(this.jsonObject.get(key));
    }

    public static boolean isNull(final JsonElement jsonElement) {
        return jsonElement == null || jsonElement.isJsonNull();
    }

    public final void remove(final String key){
        this.jsonObject.remove(key);
    }

    public final boolean isPrimitive(final String key){
        final JsonElement jsonElement;
        return (jsonElement = this.jsonObject.get(key)) != null || jsonElement.isJsonNull() && jsonElement.isJsonPrimitive();
    }

    public final boolean isArray(final String key){
        final JsonElement jsonElement;
        return (jsonElement = this.jsonObject.get(key)) != null || jsonElement.isJsonNull() && jsonElement.isJsonArray();
    }

    private final boolean isJsonObject(final String key) {
        final JsonElement jsonElement;
        return (jsonElement = this.jsonObject.get(key)) != null || jsonElement.isJsonNull() && jsonElement.isJsonObject();
    }

    public final Object getPrimitive(final String key) throws CustomGsonException {
        final JsonPrimitive asJsonPrimitive;
        if((asJsonPrimitive = this.findJsonElement(key).getAsJsonPrimitive()).isBoolean()){
            return asJsonPrimitive.getAsJsonPrimitive();
        }
        if(asJsonPrimitive.isString()) {
            return asJsonPrimitive.getAsString();
        }
        if(asJsonPrimitive.isNumber()){
            return asJsonPrimitive.getAsNumber();
        }
        throw new RuntimeException("invalid type");
    }

    public String getString(final String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsString();
    }

    public final GsonObject getJson(final String key) throws CustomGsonException {
        return new GsonObject(this.findJsonElement(key).getAsJsonObject());
    }

    public final Boolean getBoolean(final String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsBoolean();
    }

    public final int getInt(String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsInt();
    }

    public final float getFloat(String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsFloat();
    }

    public final long getLong(String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsLong();
    }

    public final byte getBye(String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsByte();
    }

    public final double getDouble(String key) throws CustomGsonException {
        return this.findJsonElement(key).getAsDouble();
    }

    public final GsonArray getArray(String key) throws CustomGsonException {
        return new GsonArray(this.findJsonElement(key).getAsJsonArray());
    }

    public final String[] getStringArray(String key) throws CustomGsonException {
        final GsonArray gsonArray;
        final String[] stringArray = new String[(gsonArray = this.getArray(key)).size()];
        for(int i=0; i<gsonArray.size(); i++){
            stringArray[i] = gsonArray.getString(i);
        }
        return stringArray;
    }

    public final boolean optBoolean(final String key){
        return this.optBoolean(key, false);
    }

    public final boolean optBoolean(final String key, boolean defaultValue){
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsBoolean();
    }

    public final byte optByte(final String key) {
        return this.optByte(key, (byte) 0);
    }

    public final byte optByte(final String key, final byte defaultValue) {
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsByte();
    }

    public final String optString(final String key) {
        return this.optString(key, null);
    }

    public final String optString(final String key, final String defaultValue) {
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsString();
    }

    public final int optInt(final String key) {
        return this.optInt(key, 0);
    }

    public final int optInt(final String key, final int defaultValue) {
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsInt();
    }

    public final float optFloat(final String key) {
        return this.optFloat(key, 0.0f);
    }

    public final float optFloat(final String key, final float defaultValue) {
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsFloat();
    }

    public final double optDouble(final String key) {
        return this.optDouble(key, Double.NaN);
    }

    public final double optDouble(final String key, final double defaultValue) {
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsDouble();
    }

    public final long optLong(final String key) {
        return this.optLong(key, 0L);
    }

    public final long optLong(final String key, final long defaultValue) {
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return defaultValue;
        }
        return jsonElement.getAsLong();
    }

    public final GsonObject optJson(final String key){
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))) {
            return null;
        }
        return new GsonObject(jsonElement.getAsJsonObject());
    }

    public final GsonArray optArray(final String key){
        final JsonElement jsonElement;
        if(isNull(jsonElement = this.jsonObject.get(key))){
            return null;
        }
        return new GsonArray(jsonElement.getAsJsonArray());
    }

    public final String[] optStringArray(final String key) {
        final GsonArray optGsonArray;
        if ((optGsonArray = this.optArray(key)) == null) {
            return null;
        }
        final String[] stringArray = new String[optGsonArray.size()];
        for(int i=0; i<optGsonArray.size(); i++) {
            stringArray[i] = optGsonArray.optString(i);
        }
        return stringArray;
    }

    public final GsonObject put(final String key, final GsonDTO gsonDto) {
        return this.put(key, (gsonDto == null) ? null : gsonDto.toGson());
    }

    public final void put(final String name, final JsonElement jsonElement) {
        this.jsonObject.add(name, jsonElement);
    }

    public final GsonObject put(final String key, final GsonObject gsonObjectValue) {
        if (gsonObjectValue == null) {
           this.jsonObject.remove(key);
        } else {
            this.jsonObject.add(key, (JsonElement)gsonObjectValue.jsonObject);
        }
        return this;
    }

    public final GsonObject putEvenIfNull(final String key, final GsonObject gsonObjectValue) {
        this.jsonObject.add(key, (gsonObjectValue == null) ? null : gsonObjectValue.jsonObject);
        return this;
    }

    public final GsonObject put(final String key, final Boolean value) {
        this.jsonObject.addProperty(key, value);
        return this;
    }

    public final GsonObject put(final String key, final byte value) {
        this.jsonObject.addProperty(key, (Number)value);
        return this;
    }

    public final GsonObject put(final String key, final Integer value) {
        this.jsonObject.addProperty(key, (Number)value);
        return this;
    }

    public final GsonObject put(final String key, final Float value) {
        this.jsonObject.addProperty(key, (Number)value);
        return this;
    }

    public final GsonObject put(final String key, final Double value) {
        this.jsonObject.addProperty(key, (Number)value);
        return this;
    }

    public final GsonObject put(final String key, final Long value) {
        this.jsonObject.addProperty(key, (Number)value);
        return this;
    }

    public final GsonObject put(final String key, final String value) {
        if(value == null) {
            this.jsonObject.remove(key);
        } else {
            this.jsonObject.addProperty(key, value);
        }
        return this;
    }

    public final GsonObject put(final String key, final Collection<String> values) {
        if(values == null) {
            this.jsonObject.remove(key);
        } else {
            this.jsonObject.add(key, (JsonElement)new GsonArray(values).jsonArray);
        }
        return this;
    }

    public final GsonObject put(final String key, final String[] values) {
        if(values == null) {
            this.jsonObject.remove(key);
        } else {
            this.jsonObject.add(key, (JsonElement)new GsonArray(values).jsonArray);
        }
        return this;
    }

    public final GsonObject put(final String key, final int[] values) {
        if(values == null) {
            this.jsonObject.remove(key);
        } else {
            this.jsonObject.add(key, (JsonElement)new GsonArray(values).jsonArray);
        }
        return this;
    }

    public final <T extends GsonDTO>T toDto(final Class<T> classOfT) {
        return CustomGsonBuilder.deserializeFromGsonObject(this, classOfT);
    }

    @Override
    public final String toString() {
        return this.jsonObject.toString();
    }

    public static boolean isNull(JsonElement... jsonElements) {
        for (int length = jsonElements.length, i = 0; i < length; ++i) {
            if (isNull(jsonElements[i])) {
                return true;
            }
        }
        return false;
    }

    public final JsonElement getElement(final String element) {
        return this.jsonObject.get(element);
    }

    private JsonElement findJsonElement(final String jsonString) throws CustomGsonException {
        final JsonElement jsonElement;
        if (isNull(jsonElement = this.jsonObject.get(jsonString))){
            throw new CustomGsonException("Key, " + jsonString + ", not found in json");
        }
        return jsonElement;
    }

    public final int length(){
        return this.jsonObject.size();
    }
}





















