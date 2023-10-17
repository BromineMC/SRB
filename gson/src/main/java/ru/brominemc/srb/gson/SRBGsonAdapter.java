/*
 * Copyright 2023 BromineMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.brominemc.srb.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.brominemc.srb.Language;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GSON adapter (deserializer) for {@link Language}.
 *
 * @author VidTu
 */
public final class SRBGsonAdapter implements JsonDeserializer<Language> {
    @Override
    public Language deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        if (!(element instanceof JsonObject json)) {
            throw new JsonParseException("Language must be a JSON object: " + element);
        }
        String id = getString(json, "id");
        String name = getString(json, "name");
        JsonArray array = getArray(json, "ids");
        Set<String> ids = HashSet.newHashSet(array.size());
        for (JsonElement entry : array) {
            String idsId;
            try {
                idsId = entry.getAsString();
            } catch (Exception e) {
                throw new JsonParseException("Expected to have all strings in 'ids', got '" + entry + "': " + json, e);
            }
            ids.add(idsId);
        }
        array = getArray(json, "authors");
        List<String> authors = new ArrayList<>(array.size());
        for (JsonElement entry : array) {
            String author;
            try {
                author = entry.getAsString();
            } catch (Exception e) {
                throw new JsonParseException("Expected to have all strings in 'authors', got '" + entry + "': " + json, e);
            }
            authors.add(author);
        }
        JsonObject dataObject = getObject(json, "data");
        Map<String, List<String>> data = HashMap.newHashMap(dataObject.size());
        for (Map.Entry<String, JsonElement> entry : dataObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                data.put(key, List.of(value.getAsString()));
                continue;
            }
            if (value.isJsonArray()) {
                array = value.getAsJsonArray();
                List<String> lines = new ArrayList<>(array.size());
                for (JsonElement subEntry : array) {
                    String line;
                    try {
                        line = subEntry.getAsString();
                    } catch (Exception e) {
                        throw new JsonParseException("Expected to have all strings in 'data -> " + key + "', got '" + entry + "': " + json, e);
                    }
                    lines.add(line);
                }
                data.put(key, lines);
                continue;
            }
            throw new JsonParseException("Expected to have string or array of string in 'data -> " + key + "', got '" + entry + "' (" + entry.getClass().getName() + "): " + json);
        }
        String rawShortDateTime = getString(json, "shortDateTime");
        DateTimeFormatter shortDateTime;
        try {
            shortDateTime = DateTimeFormatter.ofPattern(rawShortDateTime);
        } catch (Exception e) {
            throw new JsonParseException("Expected to have valid date-time pattern in 'shortDateTime', got '" + rawShortDateTime + "': " + json, e);
        }
        String rawFullDateTime = getString(json, "fullDateTime");
        DateTimeFormatter fullDateTime;
        try {
            fullDateTime = DateTimeFormatter.ofPattern(rawFullDateTime);
        } catch (Exception e) {
            throw new JsonParseException("Expected to have valid date-time pattern in 'fullDateTime', got '" + rawFullDateTime + "': " + json, e);
        }
        return new Language(id, name, ids, authors, data, shortDateTime, fullDateTime);
    }

    @Contract(pure = true)
    @NotNull
    private static String getString(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            throw new JsonParseException("Expected to have string '" + key + "': " + json, e);
        }
    }

    @Contract(pure = true)
    @NotNull
    private static JsonArray getArray(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsJsonArray();
        } catch (Exception e) {
            throw new JsonParseException("Expected to have array '" + key + "': " + json, e);
        }
    }

    @Contract(pure = true)
    @NotNull
    private static JsonObject getObject(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsJsonObject();
        } catch (Exception e) {
            throw new JsonParseException("Expected to have object '" + key + "': " + json, e);
        }
    }
}
