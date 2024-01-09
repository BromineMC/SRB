/*
 * Copyright 2023-2024 BromineMC
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Gson adapter (deserializer) for {@link Language}.
 *
 * @author VidTu
 * @author threefusii
 */
public final class SRBGsonAdapter implements JsonDeserializer<Language> {
    /**
     * Creates a new adapter.
     */
    public SRBGsonAdapter() {
        // Empty
    }

    @Override
    public Language deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) {
        // Require to be JSON.
        if (!(element instanceof JsonObject json)) {
            throw new JsonParseException("Language must be a JSON object: " + element);
        }

        // Extract ID and name.
        String id = getString(json, "id");
        String name = getString(json, "name");

        // Extract the locale, if any.
        // TODO(threefusii): Make mandatory in 2.0.0.
        Locale locale;
        if (json.has("locale")) {
            String rawLocale = getString(json, "locale");
            locale = Locale.forLanguageTag(rawLocale);
        } else {
            locale = Locale.getDefault();
        }

        // Extract the IDs.
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

        // Extract the authors.
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

        // Extract the data.
        JsonObject dataObject = getObject(json, "data");
        Map<String, List<String>> data = HashMap.newHashMap(dataObject.size());
        for (Map.Entry<String, JsonElement> entry : dataObject.entrySet()) {
            // Extract the entry.
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            // JSON primitive - one line.
            if (value.isJsonPrimitive()) {
                data.put(key, List.of(value.getAsString()));
                continue;
            }

            // Array - multiline.
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

            // Unknown type.
            throw new JsonParseException("Expected to have string or array of string in 'data -> " + key + "', got '" + entry + "' (" + entry.getClass().getName() + "): " + json);
        }

        // Extract the short date-time formatter.
        String rawShortDateTime = getString(json, "shortDateTime");
        DateTimeFormatter shortDateTime;
        try {
            shortDateTime = DateTimeFormatter.ofPattern(rawShortDateTime, locale);
        } catch (Exception e) {
            throw new JsonParseException("Expected to have valid date-time pattern in 'shortDateTime', got '" + rawShortDateTime + "': " + json, e);
        }

        // Extract the long date-time formatter.
        String rawFullDateTime = getString(json, "fullDateTime");
        DateTimeFormatter fullDateTime;
        try {
            fullDateTime = DateTimeFormatter.ofPattern(rawFullDateTime, locale);
        } catch (Exception e) {
            throw new JsonParseException("Expected to have valid date-time pattern in 'fullDateTime', got '" + rawFullDateTime + "': " + json, e);
        }

        // Return the language.
        return new Language(id, name, locale, ids, authors, data, shortDateTime, fullDateTime);
    }

    /**
     * Extracts the string from the JSON.
     *
     * @param json Target JSON object
     * @param key  Target key
     * @return Extracted string
     * @throws JsonParseException If unable to extract the string
     */
    @Contract(pure = true)
    @NotNull
    private static String getString(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            throw new JsonParseException("Expected to have string '" + key + "': " + json, e);
        }
    }

    /**
     * Extracts the array from the JSON.
     *
     * @param json Target JSON object
     * @param key  Target key
     * @return Extracted array
     * @throws JsonParseException If unable to extract the array
     */
    @Contract(pure = true)
    @NotNull
    private static JsonArray getArray(@NotNull JsonObject json, @NotNull String key) {
        try {
            return json.get(key).getAsJsonArray();
        } catch (Exception e) {
            throw new JsonParseException("Expected to have array '" + key + "': " + json, e);
        }
    }

    /**
     * Extracts the object from the JSON.
     *
     * @param json Target JSON object
     * @param key  Target key
     * @return Extracted object
     * @throws JsonParseException If unable to extract the object
     */
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
