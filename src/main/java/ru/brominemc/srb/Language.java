package ru.brominemc.srb;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Language instance.
 *
 * @author VidTu
 */
public class Language {
    // Original data
    protected final String id;
    protected final String name;
    protected final Set<String> ids;
    protected final List<String> authors;
    protected final Map<String, List<String>> data;
    protected final DateTimeFormatter shortDateTime;
    protected final DateTimeFormatter fullDateTime;

    // Caches
    protected final Map<String, List<String>> linesCache;
    protected final Map<String, String> lineCache;

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
     */
    public Language(@NotNull String id, @NotNull String name, @NotNull Set<String> ids,
                    @NotNull List<String> authors, @NotNull Map<String, List<String>> data,
                    @NotNull DateTimeFormatter shortDateTime, @NotNull DateTimeFormatter fullDateTime) {
        // These are immutable
        this.id = id.intern();
        this.name = name.intern();
        this.shortDateTime = shortDateTime;
        this.fullDateTime = fullDateTime;

        this.ids = ids.stream().map(String::intern).collect(Collectors.toUnmodifiableSet()); // Copy and intern IDs (ensures immutability and null-safety)
        this.authors = authors.stream().map(String::intern).toList(); // Copy and intern authors (ensures immutability and null-safety)

        // Deep copy data (ensures immutability and null-safety, including that of nested lists)
        int capacity = (int) Math.ceil(data.size() / 0.75d);
        HashMap<String, List<String>> newData = new HashMap<>(capacity);
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            List<String> newValue = new ArrayList<>(value.size());
            for (String line : value) {
                int index = line.indexOf('\n');
                if (index != -1) {
                    throw new IllegalArgumentException("Line contains LF (\\n): " + line.replace("\n", "\\n").replace("\r", "\\r"));
                }
                index = line.indexOf('\r');
                if (index != -1) {
                    throw new IllegalArgumentException("Line contains CR (\\r): " + line.replace("\n", "\\n").replace("\r", "\\r"));
                }
                newValue.add(line.intern());
            }
            newData.put(key, List.copyOf(newValue));
        }
        this.data = Map.copyOf(newData);

        // Create caches
        this.linesCache = new ConcurrentHashMap<>(capacity);
        this.lineCache = new ConcurrentHashMap<>(capacity);
    }

    /**
     * Gets the language ID.
     *
     * @return Language ID
     */
    @Contract(pure = true)
    @NotNull
    public final String id() {
        return id;
    }

    /**
     * Gets the language display name.
     *
     * @return Language name
     */
    @Contract(pure = true)
    @NotNull
    public String name() {
        return name;
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
        return ids;
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
        return authors;
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
        return data;
    }

    /**
     * Gets the short date time formatter.
     *
     * @return Short formatter
     */
    @Contract(pure = true)
    @NotNull
    public final DateTimeFormatter shortDateTime() {
        return shortDateTime;
    }

    /**
     * Gets the long date time formatter.
     *
     * @return Long formatter
     */
    @Contract(pure = true)
    @NotNull
    public final DateTimeFormatter fullDateTime() {
        return fullDateTime;
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
        return linesCache.computeIfAbsent(key, k -> {
            List<String> lines = data.get(key);
            if (lines == null || lines.isEmpty()) {
                SRB.platform().logger().log(System.Logger.Level.WARNING, "Unable to get key from lang %s: %s", id, key);
                return List.of(key.intern());
            }
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
        return lineCache.computeIfAbsent(key, k -> {
            List<String> lines = data.get(key);
            if (lines == null || lines.isEmpty()) {
                SRB.platform().logger().log(System.Logger.Level.WARNING, "Unable to get key from lang %s: %s", id, key);
                return key.intern();
            }
            return String.join("\n", lines).intern();
        });
    }

    /**
     * Formats the date-time into short human-readable format.
     *
     * @param temporal Target datetime
     * @return Formatted date-time
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public final String shortDateTime(@NotNull TemporalAccessor temporal) {
        return shortDateTime.format(temporal);
    }

    /**
     * Formats the date-time into precise human-readable format.
     *
     * @param temporal Target datetime
     * @return Formatted date-time
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public final String fullDateTime(@NotNull ZonedDateTime temporal) {
        return fullDateTime.format(temporal);
    }

    /**
     * Formats the duration into human-readable format.
     *
     * @param duration Target duration
     * @param precise  Should millis be always shown
     * @return Formatted duration
     */
    @Contract("_, _ -> new")
    @CheckReturnValue
    @NotNull
    public String duration(@NotNull Duration duration, boolean precise) {
        if (duration.isNegative()) return line("unknown");
        int millis = duration.toMillisPart();
        int seconds = duration.toSecondsPart();
        int minutes = duration.toMinutesPart();
        int hours = duration.toHoursPart();
        long days = duration.toDaysPart();
        String key;
        if (days > 0L) {
            key = "days";
        } else if (hours > 0) {
            key = "hours";
        } else if (minutes > 0) {
            key = "minutes";
        } else if (seconds > 0 || !precise) {
            key = "seconds";
        } else {
            key = "millis";
        }
        if (precise) {
            key = key.concat(".precise");
        }
        return line(key)
                .replace("%days%", Long.toString(days))
                .replace("%hours%", Integer.toString(hours))
                .replace("%minutes%", Integer.toString(minutes))
                .replace("%seconds%", Integer.toString(seconds))
                .replace("%millis%", Integer.toString(millis));
    }

    /**
     * Tries to pre-fill all caches.
     *
     * @apiNote You are not required to call this method, though calling it might improve performance of {@link #line(String)} and {@link #lines(String)}
     */
    public void preCache() {
        for (String key : data.keySet()) {
            List<String> ignoredLines = lines(key);
            String ignoredLine = line(key);
        }
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
        if (id == null) return ofDefault();
        return Objects.requireNonNullElseGet(SRB.platform().language(id), Language::ofDefault);
    }

    /**
     * Gets the language via {@link SRBPlatform#of(Object)}, falling back to default language if not found or receiver not supported.
     *
     * @param receiver Target receiver
     * @return Language by ID, {@link #ofDefault()} if language not found or receiver not supported
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is null and the platform chose not to return {@code null}
     * @see SRBPlatform#of(Object)
     * @see #ofId(String)
     */
    @CheckReturnValue
    @NotNull
    public static Language ofReceiver(@Nullable Object receiver) {
        return Objects.requireNonNullElseGet(SRB.platform().of(receiver), Language::ofDefault);
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
    @Unmodifiable
    public static String lineOfId(@Nullable String id, @NotNull String key) {
        return ofId(id).line(key);
    }

    /**
     * Gets localized string lines from {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized string lines, singleton {@code key} if not found
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is null and the platform chose not to return {@code null}
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
     * @throws NullPointerException     If receiver is null and the platform chose not to return {@code null}
     * @apiNote This method will join lines with LF ({@code \n}) if multiple are found
     * @see #ofReceiver(Object)
     * @see #line(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static String lineOfReceiver(@Nullable Object receiver, @NotNull String key) {
        return ofReceiver(receiver).line(key);
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
    @Unmodifiable
    public static String lineOfDefault(@NotNull String key) {
        return ofDefault().line(key);
    }
}