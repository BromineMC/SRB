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

package ru.brominemc.srb;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Language instance.
 *
 * @author VidTu
 */
public class Language {
    /**
     * Language ID.
     */
    protected final String id;

    /**
     * Language name.
     */
    protected final String name;

    /**
     * Language locale.
     */
    protected final Locale locale;

    /**
     * Unmodifiable language IDs.
     */
    protected final Set<String> ids;

    /**
     * Unmodifiable language authors.
     */
    protected final List<String> authors;

    /**
     * Unmodifiable language data.
     */
    protected final Map<String, List<String>> data;

    /**
     * Language short time format.
     */
    protected final DateTimeFormatter shortDateTime;

    /**
     * Language full time format.
     */
    protected final DateTimeFormatter fullDateTime;

    /**
     * Cache for lists of lines.
     */
    protected final Map<String, List<String>> linesCache;

    /**
     * Cache for one-lines.
     */
    protected final Map<String, String> lineCache;

    /**
     * Cache for custom objects.
     */
    protected final Map<Object, Object> customCache;

    /**
     * Creates a new language.
     *
     * @param id            Language ID, should be non-null and unique
     * @param name          Language display name, should be non-null
     * @param ids           Language identification IDs, should be non-null and should not contain null elements
     * @param authors       Language authors, should be non-null and should not contain null elements
     * @param data          Language keys mapped to lists of lines (lists should not contain null elements or elements with {@code \n} or {@code \r} characters), should be not null
     * @param shortDateTime Short date-time formatter
     * @param fullDateTime  Full (precise) date-time formatter
     * @throws NullPointerException If any of the objects or entries is {@code null}
     * @deprecated Use {@link #Language(String, String, Locale, Set, List, Map, DateTimeFormatter, DateTimeFormatter)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated(since = "1.2.0", forRemoval = true)
    public Language(@NotNull String id, @NotNull String name, @NotNull Set<String> ids,
                    @NotNull List<String> authors, @NotNull Map<String, List<String>> data,
                    @NotNull DateTimeFormatter shortDateTime, @NotNull DateTimeFormatter fullDateTime) {
        this(id, name, Locale.getDefault(), ids, authors, data, shortDateTime, fullDateTime);
    }

    /**
     * Creates a new language.
     *
     * @param id            Language ID, should be non-null and unique
     * @param name          Language display name, should be non-null
     * @param ids           Language identification IDs, should be non-null and should not contain null elements
     * @param locale        Language locale, should be non-null
     * @param authors       Language authors, should be non-null and should not contain null elements
     * @param data          Language keys mapped to lists of lines (lists should not contain null elements or elements with {@code \n} or {@code \r} characters), should be not null
     * @param shortDateTime Short date-time formatter
     * @param fullDateTime  Full (precise) date-time formatter
     * @throws NullPointerException If any of the objects or entries is {@code null}
     */
    public Language(@NotNull String id, @NotNull String name, @NotNull Locale locale, @NotNull Set<String> ids,
                    @NotNull List<String> authors, @NotNull Map<String, List<String>> data,
                    @NotNull DateTimeFormatter shortDateTime, @NotNull DateTimeFormatter fullDateTime) {
        // Copy and intern basic data.
        this.id = id.intern();
        this.name = name.intern();
        this.locale = Objects.requireNonNull(locale, "locale is null");
        this.shortDateTime = Objects.requireNonNull(shortDateTime, "shortDateTime is null");
        this.fullDateTime = Objects.requireNonNull(fullDateTime, "fullDateTime is null");

        // Copy and intern IDs. (ensures immutability and null-safety)
        this.ids = Set.copyOf(ids.stream()
                .map(String::intern)
                .collect(Collectors.toUnmodifiableSet()));

        // Copy and intern authors. (ensures immutability and null-safety)
        this.authors = List.copyOf(authors.stream()
                .map(String::intern)
                .toList());

        // Deep copy data. (ensures immutability and null-safety, including that of nested lists)
        Map<String, List<String>> newData = HashMap.newHashMap(data.size());

        // Iterate over every entry.
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            // Extract the entry.
            String key = entry.getKey();
            List<String> value = entry.getValue();

            // Copy the list.
            value = List.copyOf(value.stream()
                    .map(String::intern)
                    .toList());

            // Put the entry back.
            newData.put(key.intern(), value);
        }

        // Flush the map.
        this.data = Map.copyOf(newData);

        // Create caches
        int capacity = (int) Math.ceil(data.size() / 0.75d);
        this.linesCache = new ConcurrentHashMap<>(capacity);
        this.lineCache = new ConcurrentHashMap<>(capacity);
        this.customCache = new ConcurrentHashMap<>(0);
    }

    /**
     * Gets the language ID.
     *
     * @return Language ID
     */
    @Contract(pure = true)
    @NotNull
    public final String id() {
        return this.id;
    }

    /**
     * Gets the language display name.
     *
     * @return Language name
     */
    @Contract(pure = true)
    @NotNull
    public String name() {
        return this.name;
    }

    /**
     * Gets the language locale.
     *
     * @return Language locale
     */
    @Contract(pure = true)
    @NotNull
    public final Locale locale() {
        return this.locale;
    }

    /**
     * Gets the immutable set of all IDs this language is searchable with.
     *
     * @return Language IDs
     */
    @Contract(pure = true)
    @NotNull
    @Unmodifiable
    public final Set<String> ids() {
        return this.ids;
    }

    /**
     * Gets the unmodifiable list of language authors. (translators)
     *
     * @return Language authors
     */
    @Contract(pure = true)
    @NotNull
    @Unmodifiable
    public final List<String> authors() {
        return this.authors;
    }

    /**
     * Gets the unmodifiable map of language keys mapped to unmodifiable lists of lines.
     *
     * @return Language data
     */
    @Contract(pure = true)
    @NotNull
    @Unmodifiable
    public final Map<String, List<String>> data() {
        return this.data;
    }

    /**
     * Gets the short date time formatter.
     *
     * @return Short formatter
     */
    @Contract(pure = true)
    @NotNull
    public final DateTimeFormatter shortDateTime() {
        return this.shortDateTime;
    }

    /**
     * Gets the long date time formatter.
     *
     * @return Long formatter
     */
    @Contract(pure = true)
    @NotNull
    public final DateTimeFormatter fullDateTime() {
        return this.fullDateTime;
    }

    /**
     * Gets localized string lines.
     *
     * @param key Localization key
     * @return Localized string lines, singleton {@code key} if not found
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public final List<String> lines(@NotNull String key) {
        // Get from cache.
        return this.linesCache.computeIfAbsent(key, k -> {
            // Compute cached value.
            List<String> lines = this.data.get(key);

            // Flush no lines.
            if (lines == null || lines.isEmpty()) {
                return List.copyOf(SRB.platform().missingKey(this, key).stream()
                        .map(String::intern)
                        .toList());
            }

            // Flush to cache.
            return lines;
        });
    }


    /**
     * Gets a localized string.
     *
     * @param key Localization key
     * @return Localized string, {@code key} if not found
     * @apiNote This method will join lines with LF ({@code \n}) if multiple are found
     */
    @CheckReturnValue
    @NotNull
    public final String line(@NotNull String key) {
        // Get from cache.
        return this.lineCache.computeIfAbsent(key, k -> {
            // Compute cached value.
            List<String> lines = this.data.get(key);

            // Flush no lines.
            if (lines == null || lines.isEmpty()) {
                return String.join("\n", SRB.platform().missingKey(this, key).stream()
                        .map(String::intern)
                        .toList()).intern();
            }

            // Flush to cache.
            return String.join("\n", lines).intern();
        });
    }

    /**
     * Formats the date-time into short human-readable format.
     *
     * @param temporal Target dat-etime
     * @return Formatted date-time
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public final String shortDateTime(@NotNull TemporalAccessor temporal) {
        return this.shortDateTime.format(temporal);
    }

    /**
     * Formats the date-time into precise human-readable format.
     *
     * @param temporal Target date-time
     * @return Formatted date-time
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public final String fullDateTime(@NotNull TemporalAccessor temporal) {
        return this.fullDateTime.format(temporal);
    }

    /**
     * Formats the duration into human-readable format.
     *
     * @param duration Target duration
     * @param precise  Should millis be always shown
     * @return Formatted duration
     */
    @CheckReturnValue
    @NotNull
    public String duration(@NotNull Duration duration, boolean precise) {
        // Return "unknown" for negative duration.
        if (duration.isNegative()) {
            return this.line("unknown");
        }

        // Extract the parts.
        int millis = duration.toMillisPart();
        int seconds = duration.toSecondsPart();
        int minutes = duration.toMinutesPart();
        int hours = duration.toHoursPart();
        long days = duration.toDaysPart();

        // Create the key.
        String key;
        if (days > 0L) {
            key = precise ? "days.precise" : "days";
        } else if (hours > 0) {
            key = precise ? "hours.precise" : "hours";
        } else if (minutes > 0) {
            key = precise ? "minutes.precise" : "minutes";
        } else if (seconds > 0 || !precise) {
            key = precise ? "seconds.precise" : "seconds";
        } else {
            key = "millis";
        }

        // Get and replace key values.
        return this.line(key)
                .replace("%days%", Long.toString(days))
                .replace("%hours%", Integer.toString(hours))
                .replace("%minutes%", Integer.toString(minutes))
                .replace("%seconds%", Integer.toString(seconds))
                .replace("%millis%", Integer.toString(millis));
    }

    /**
     * Gets the custom cached object for this language.
     *
     * @param key    Target key
     * @param loader Loader for missing value
     * @param <T>    Type of stored value
     * @return Cached value, will be cached from {@code loader} if absent
     * @throws ClassCastException If the stored value can't be cast to required value
     */
    @CheckReturnValue
    @NotNull
    public <T> T custom(@Nullable Object key, @NotNull Supplier<T> loader) {
        @SuppressWarnings("unchecked")
        T value = (T) this.customCache.computeIfAbsent(key, k -> loader.get());
        return value;
    }

    /**
     * Tries to pre-fill all caches.
     *
     * @apiNote You are not required to call this method, though calling it might improve performance of {@link #line(String)} and {@link #lines(String)}
     */
    @OverridingMethodsMustInvokeSuper
    public void preCache() {
        // Iterate over every key.
        for (String key : this.data.keySet()) {
            // Get the lines and single lines.
            List<String> ignoredLines = this.lines(key);
            String ignoredLine = this.line(key);
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Language that = (Language) obj;
        return this.id.equals(that.id) && this.name.equals(that.name) && this.locale.equals(that.locale) &&
                this.ids.equals(that.ids) && this.authors.equals(that.authors) &&
                this.shortDateTime.equals(that.shortDateTime) && this.fullDateTime.equals(that.fullDateTime) &&
                this.data.equals(that.data);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.locale, this.ids, this.authors, this.data, this.shortDateTime, this.fullDateTime);
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "Language{" +
                "id='" + this.id + '\'' +
                ", name='" + this.name + '\'' +
                ", locale=" + this.locale +
                ", ids=" + this.ids +
                ", authors=" + this.authors +
                ", data=" + this.data +
                ", shortDateTime=" + this.shortDateTime +
                ", fullDateTime=" + this.fullDateTime +
                ", linesCache=" + this.linesCache +
                ", lineCache=" + this.lineCache +
                ", customCache=" + this.customCache +
                '}';
    }

    /**
     * Gets the language by its ID via {@link SRBPlatform#language(String)}, falling back to default language if not found.
     *
     * @param id Target ID
     * @return Language by ID, {@link #ofDefault()} if {@code id} is null or language not found
     * @see SRBPlatform#language(String)
     * @see #ofDefault()
     */
    @CheckReturnValue
    @NotNull
    public static Language ofId(@Nullable String id) {
        // Get the platform.
        SRBPlatform platform = SRB.platform();

        // Return default for null.
        if (id == null) return platform.defaultLanguage();

        // Return language of ID or default.
        return Objects.requireNonNullElseGet(platform.language(id), platform::defaultLanguage);
    }

    /**
     * Gets the language via {@link SRBPlatform#of(Object)}, falling back to default language if not found or receiver not supported.
     *
     * @param receiver Target receiver
     * @return Language from receiver, {@link #ofDefault()} if language not found or receiver not supported
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see SRBPlatform#of(Object)
     * @see #ofId(String)
     */
    @CheckReturnValue
    @NotNull
    public static Language ofReceiver(@Nullable Object receiver) {
        // Get the platform.
        SRBPlatform platform = SRB.platform();

        // Return language of receiver or default.
        return Objects.requireNonNullElseGet(platform.of(receiver), platform::defaultLanguage);
    }

    /**
     * Gets the {@link SRBPlatform#defaultLanguage()}.
     *
     * @return Default language
     * @see SRBPlatform#defaultLanguage()
     */
    @CheckReturnValue
    @NotNull
    public static Language ofDefault() {
        // Return default language.
        return SRB.platform().defaultLanguage();
    }

    /**
     * Gets localized string lines from {@link #ofId(String)} language.
     *
     * @param id  Target language ID
     * @param key Localization key
     * @return Localized string lines, singleton {@code key} if not found
     * @see #ofId(String)
     * @see #lines(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<String> linesOfId(@Nullable String id, @NotNull String key) {
        return ofId(id).lines(key);
    }

    /**
     * Gets a localized string from {@link #ofId(String)} language.
     *
     * @param id  Target language ID
     * @param key Localization key
     * @return Localized string, {@code key} if not found
     * @apiNote This method will join lines with LF ({@code \n}) if multiple are found
     * @see #ofId(String)
     * @see #line(String)
     */
    @CheckReturnValue
    @NotNull
    public static String lineOfId(@Nullable String id, @NotNull String key) {
        return ofId(id).line(key);
    }

    /**
     * Formats the date-time into short human-readable format via {@link #ofId(String)} language.
     *
     * @param id       Target language ID
     * @param temporal Target date-time
     * @return Formatted date-time
     * @see #ofId(String)
     * @see #shortDateTime(TemporalAccessor)
     */
    @CheckReturnValue
    @NotNull
    public static String shortDateTimeOfId(@Nullable String id, @NotNull TemporalAccessor temporal) {
        return ofId(id).shortDateTime(temporal);
    }

    /**
     * Formats the date-time into precise human-readable format via {@link #ofId(String)} language.
     *
     * @param id       Target language ID
     * @param temporal Target date-time
     * @return Formatted date-time
     * @see #ofId(String)
     * @see #fullDateTime(TemporalAccessor)
     */
    @CheckReturnValue
    @NotNull
    public static String fullDateTimeOfId(@Nullable String id, @NotNull TemporalAccessor temporal) {
        return ofId(id).fullDateTime(temporal);
    }

    /**
     * Formats the duration into human-readable format via {@link #ofId(String)} language.
     *
     * @param id       Target language ID
     * @param duration Target duration
     * @param precise  Should millis be always shown
     * @return Formatted duration
     * @see #ofId(String)
     * @see #duration(Duration, boolean)
     */
    @CheckReturnValue
    @NotNull
    public static String durationOfId(@NotNull String id, @NotNull Duration duration, boolean precise) {
        return ofId(id).duration(duration, precise);
    }

    /**
     * Gets the custom cached object for {@link #ofId(String)} language.
     *
     * @param id     Target language ID
     * @param key    Target key
     * @param loader Loader for missing value
     * @param <T>    Type of stored value
     * @return Cached value, will be cached from {@code loader} if absent
     * @see #ofId(String)
     * @see #custom(Object, Supplier)
     */
    @CheckReturnValue
    @NotNull
    public static <T> T customOfId(@NotNull String id, @Nullable Object key, @NotNull Supplier<T> loader) {
        return ofId(id).custom(key, loader);
    }

    /**
     * Gets localized string lines from {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized string lines, singleton {@code key} if not found
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiver(Object)
     * @see #lines(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<String> linesOfReceiver(@Nullable Object receiver, @NotNull String key) {
        return ofReceiver(receiver).lines(key);
    }

    /**
     * Gets a localized string from {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized string, {@code key} if not found
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @apiNote This method will join lines with LF ({@code \n}) if multiple are found
     * @see #ofReceiver(Object)
     * @see #line(String)
     */
    @CheckReturnValue
    @NotNull
    public static String lineOfReceiver(@Nullable Object receiver, @NotNull String key) {
        return ofReceiver(receiver).line(key);
    }

    /**
     * Formats the date-time into short human-readable format via {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param temporal Target date-time
     * @return Formatted date-time
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiver(Object)
     * @see #shortDateTime(TemporalAccessor)
     */
    @CheckReturnValue
    @NotNull
    public static String shortDateTimeOfReceiver(@Nullable Object receiver, @NotNull TemporalAccessor temporal) {
        return ofReceiver(receiver).shortDateTime(temporal);
    }

    /**
     * Formats the date-time into precise human-readable format via {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param temporal Target date-time
     * @return Formatted date-time
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiver(Object)
     * @see #fullDateTime(TemporalAccessor)
     */
    @CheckReturnValue
    @NotNull
    public static String fullDateTimeOfReceiver(@Nullable Object receiver, @NotNull TemporalAccessor temporal) {
        return ofReceiver(receiver).fullDateTime(temporal);
    }

    /**
     * Formats the duration into human-readable format via {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param duration Target duration
     * @param precise  Should millis be always shown
     * @return Formatted duration
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiver(Object)
     * @see #duration(Duration, boolean)
     */
    @CheckReturnValue
    @NotNull
    public static String durationOfReceiver(@Nullable Object receiver, @NotNull Duration duration, boolean precise) {
        return ofReceiver(receiver).duration(duration, precise);
    }

    /**
     * Gets the custom cached object for {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Target key
     * @param loader   Loader for missing value
     * @param <T>      Type of stored value
     * @return Cached value, will be cached from {@code loader} if absent
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiver(Object)
     * @see #custom(Object, Supplier)
     */
    @CheckReturnValue
    @NotNull
    public static <T> T customOfReceiver(@Nullable Object receiver, @Nullable Object key, @NotNull Supplier<T> loader) {
        return ofReceiver(receiver).custom(key, loader);
    }

    /**
     * Gets localized string lines from {@link #ofDefault()} language.
     *
     * @param key Localization key
     * @return Localized string lines, singleton {@code key} if not found
     * @see #ofDefault()
     * @see #lines(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<String> linesOfDefault(@NotNull String key) {
        return ofDefault().lines(key);
    }

    /**
     * Gets a localized string from {@link #ofDefault()} language.
     *
     * @param key Localization key
     * @return Localized string, {@code key} if not found
     * @apiNote This method will join lines with LF ({@code \n}) if multiple are found
     * @see #ofDefault()
     * @see #line(String)
     */
    @CheckReturnValue
    @NotNull
    public static String lineOfDefault(@NotNull String key) {
        return ofDefault().line(key);
    }

    /**
     * Formats the date-time into short human-readable format via {@link #ofDefault()} language.
     *
     * @param temporal Target date-time
     * @return Formatted date-time
     * @see #ofDefault()
     * @see #shortDateTime(TemporalAccessor)
     */
    @CheckReturnValue
    @NotNull
    public static String shortDateTimeOfDefault(@NotNull TemporalAccessor temporal) {
        return ofDefault().shortDateTime(temporal);
    }

    /**
     * Formats the date-time into precise human-readable format via {@link #ofDefault()} language.
     *
     * @param temporal Target date-time
     * @return Formatted date-time
     * @see #ofDefault()
     * @see #fullDateTime(TemporalAccessor)
     */
    @CheckReturnValue
    @NotNull
    public static String fullDateTimeOfDefault(@NotNull TemporalAccessor temporal) {
        return ofDefault().fullDateTime(temporal);
    }

    /**
     * Formats the duration into human-readable format via {@link #ofDefault()} language.
     *
     * @param duration Target duration
     * @param precise  Should millis be always shown
     * @return Formatted duration
     * @see #ofDefault()
     * @see #duration(Duration, boolean)
     */
    @CheckReturnValue
    @NotNull
    public static String durationOfDefault(@NotNull Duration duration, boolean precise) {
        return ofDefault().duration(duration, precise);
    }

    /**
     * Gets the custom cached object for {@link #ofDefault()} language.
     *
     * @param key    Target key
     * @param loader Loader for missing value
     * @param <T>    Type of stored value
     * @return Cached value, will be cached from {@code loader} if absent
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofDefault()
     * @see #custom(Object, Supplier)
     */
    @CheckReturnValue
    @NotNull
    public static <T> T customOfDefault(@Nullable Object key, @NotNull Supplier<T> loader) {
        return ofDefault().custom(key, loader);
    }
}
