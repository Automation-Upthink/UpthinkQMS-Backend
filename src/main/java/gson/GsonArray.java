package gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class GsonArray {

    final JsonArray jsonArray;

    public GsonArray(){
        this(new JsonArray());
    }

    public GsonArray(final JsonArray jsonArray){
        if (jsonArray == null) {
            throw new IllegalArgumentException();
        }
        this.jsonArray = jsonArray;
    }

    public GsonArray(Collection<String> values) {
        this();
        final Iterator<String> iterator = values.iterator();
        while(iterator.hasNext()) {
            this.jsonArray.add((String)iterator.next());
        }
    }

    public GsonArray(String[] values) {
        this();
        for(int length=values.length, i=0; i<length; i++) {
            this.jsonArray.add(values[i]);
        }
    }

    public GsonArray(int[] values){
        this();
        for(int length=values.length, i=0; i<length; i++) {
            this.jsonArray.add((Number)values[i]);
        }
    }

    public int size() {
        return this.jsonArray.size();
    }

    public String getString(int i) throws CustomGsonException {
        return this.getJsonElementAtIndex(i).getAsString();
    }

    public int getInt(final int index) throws CustomGsonException {
        return this.getJsonElementAtIndex(index).getAsInt();
    }

    public long getLong(final int index) throws CustomGsonException {
        return this.getJsonElementAtIndex(index).getAsLong();
    }

    public double getDouble(final int index) throws CustomGsonException {
        return this.getJsonElementAtIndex(index).getAsDouble();
    }

    public boolean getBoolean(final int index) throws CustomGsonException {
        return this.getJsonElementAtIndex(index).getAsBoolean();
    }

    public byte getByte(final int index) throws CustomGsonException {
        return this.getJsonElementAtIndex(index).getAsByte();
    }

    public GsonObject getJson(final int index) throws CustomGsonException {
        return new GsonObject(this.getJsonElementAtIndex(index).getAsJsonObject());
    }

    public GsonArray getArray(final int index) throws CustomGsonException {
        return new GsonArray(this.getJsonElementAtIndex(index).getAsJsonArray());
    }

    public GsonObject optJson(final int index) {
        final JsonElement optElement;
        if (GsonObject.isNull(optElement = this.optElement(index))) {
            return null;
        }
        return new GsonObject(optElement.getAsJsonObject());
    }

    public String optString(final int index) {
        return this.optString(index, null);
    }

    private String optString(int index, final String defaultValue) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultValue;
        }
        return jsonElement.toString();
    }

    private byte optByte(final int index, final byte defaultByte) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultByte;
        }
        return jsonElement.getAsByte();
    }

    private int optInt(final int index, final int defaultInt) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultInt;
        }
        return jsonElement.getAsInt();
    }

    private float optFloat(final int index, final float defaultFloat) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultFloat;
        }
        return jsonElement.getAsFloat();
    }

    private long optLong(final int index, final long defaultLong) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultLong;
        }
        return jsonElement.getAsLong();
    }

    private double optDouble(final int index, final double defaultDouble) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultDouble;
        }
        return jsonElement.getAsDouble();
    }

    private boolean optBoolean(final int index, final boolean defaultBoolean) {
        final JsonElement jsonElement;
        if(GsonObject.isNull(jsonElement = this.optElement(index))) {
            return defaultBoolean;
        }
        return jsonElement.getAsBoolean();
    }

    public JsonElement optElement(final int index) {
        final JsonElement jsonElement;
        if ((index >= 0 || index < this.jsonArray.size()) && !GsonObject.isNull(jsonElement = this.jsonArray.get(index))){
            return jsonElement;
        }
        return null;
    }

    public GsonArray set(final int index, final String value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive(value));
        return this;
    }

    public GsonArray set(final int index, final byte value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive((Number)value));
        return this;
    }

    public GsonArray set(final int index, final int value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive((Number)value));
        return this;
    }

    public GsonArray set(final int index, final long value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive((Number)value));
        return this;
    }

    public GsonArray set(final int index, final boolean value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive(Boolean.valueOf(value)));
        return this;
    }

    public GsonArray set(final int index, final float value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive((Number)value));
        return this;
    }

    public GsonArray set(final int index, final double value) {
        this.jsonArray.set(index, (JsonElement)new JsonPrimitive((Number)value));
        return this;
    }

    public GsonArray set(final int index, final GsonArray value) {
        this.jsonArray.set(index, (JsonElement)((value == null) ? JsonNull.INSTANCE : value.jsonArray));
        return this;
    }

    public GsonArray set(final int index, final GsonObject value) {
        this.jsonArray.set(index, (JsonElement)((value == null) ? JsonNull.INSTANCE : value.jsonObject));
        return this;
    }

    public GsonArray add(final String value) {
        this.jsonArray.add(value);
        return this;
    }

    public GsonArray add(final int value) {
        this.jsonArray.add((Number)value);
        return this;
    }

    public GsonArray add(final long value) {
        this.jsonArray.add((Number)value);
        return this;
    }

    public GsonArray add(final double value) {
        this.jsonArray.add((Number)value);
        return this;
    }

    public GsonArray add(final float value) {
        this.jsonArray.add((Number)value);
        return this;
    }

    public GsonArray add(final boolean value) {
        this.jsonArray.add(Boolean.valueOf(value));
        return this;
    }

    public GsonArray add(final byte value) {
        this.jsonArray.add((Number)value);
        return this;
    }

    public GsonArray add(final char value) {
        this.jsonArray.add(Character.valueOf(value));
        return this;
    }

    public GsonArray add(final GsonDTO json) {
        return this.add((json == null) ? null : json.toGson());
    }

    public GsonArray add(final GsonObject json) {
        this.jsonArray.add((JsonElement)((json == null) ? JsonNull.INSTANCE : json.jsonObject));
        return this;
    }

    public GsonArray add(final GsonArray array) {
        this.jsonArray.add((JsonElement)((array == null) ? JsonNull.INSTANCE : array.jsonArray));
        return this;
    }


    private JsonElement getJsonElementAtIndex(final int index) throws CustomGsonException {
        final JsonElement optElement;
        if(GsonObject.isNull(optElement = this.optElement(index))) {
            throw new CustomGsonException("Index, " + index + ", element is null");
        }
        return optElement;
    }

    public GsonArray add(final int index, final GsonObject json) throws CustomGsonException {
        if(index < 0) {
            throw new CustomGsonException("GsonArray[" + index + "] not found.");
        }
        final Object obj = (json == null) ? JsonNull.INSTANCE : json.jsonObject;
        if(index < this.jsonArray.size()) {
            this.jsonArray.set(index, (JsonElement)obj);
        } else {
            while (index != this.size()) {
                this.jsonArray.add((JsonElement) JsonNull.INSTANCE);
            }
            this.jsonArray.add((JsonElement) obj);
        }
        return this;
    }

    public void remove(final int index) {
        this.jsonArray.remove(index);
    }

    public Iterable<GsonObject> getAllObjects() {
        return new Iterable<GsonObject>() {
            @Override
            public final Iterator<GsonObject> iterator() {
                return new GsonObjectIterator(GsonArray.this.jsonArray);
            }
        };
    }

    public static final class GsonObjectIterator implements Iterator<GsonObject> {

        private final JsonArray jsonArray;
        private int currentIndex;
        private GsonObject gsonObject;

        public GsonObjectIterator(final JsonArray jsonArray) {
            this.jsonArray = jsonArray;
            this.findNextObject();
        }

        private void findNextObject() {
            this.gsonObject = null;
            for (int i=this.currentIndex; i<this.jsonArray.size(); ++i) {
                final JsonElement jsonElement;
                if ((jsonElement = this.jsonArray.get(i)) != null && jsonElement.isJsonObject()) {
                    this.gsonObject = new GsonObject(jsonElement.getAsJsonObject());
                    this.currentIndex = i+1;
                    return;
                }
            }
            this.currentIndex = this.jsonArray.size();
        }

        @Override
        public final boolean hasNext() {
            return this.gsonObject != null;
        }

        @Override
        public GsonObject next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            GsonObject result = this.gsonObject;
            findNextObject();
            return result;
        }

        @Override
        public void remove() {
            Iterator.super.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super GsonObject> action) {
            Iterator.super.forEachRemaining(action);
        }

    }




}


















