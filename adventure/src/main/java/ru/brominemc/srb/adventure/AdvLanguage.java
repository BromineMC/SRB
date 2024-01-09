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

package ru.brominemc.srb.adventure;

import com.google.errorprone.annotations.CheckReturnValue;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.brominemc.srb.Language;
import ru.brominemc.srb.SRB;
import ru.brominemc.srb.SRBPlatform;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Language instance with support for Kyori Adventure API.
 *
 * @author VidTu
 */
public class AdvLanguage extends Language {
    /**
     * Cache for lists of components.
     */
    protected final Map<String, List<Component>> componentsCache;

    /**
     * Cache for single components.
     */
    protected final Map<String, Component> componentCache;

    /**
     * Creates a new Adventure language.
     *
     * @param id            Language ID, should be non-null and unique
     * @param name          Language display name, should be non-null
     * @param ids           Language identification IDs, should be non-null and should not contain null elements
     * @param authors       Language authors, should be non-null and should not contain null elements
     * @param data          Language keys mapped to lists of lines, should be not null
     * @param shortDateTime Short date-time formatter
     * @param fullDateTime  Full (precise) date-time formatter
     * @deprecated Use {@link #AdvLanguage(String, String, Locale, Set, List, Map, DateTimeFormatter, DateTimeFormatter)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated(since = "1.2.0", forRemoval = true)
    public AdvLanguage(@NotNull String id, @NotNull String name, @NotNull Set<String> ids,
                       @NotNull List<String> authors, @NotNull Map<String, List<String>> data,
                       @NotNull DateTimeFormatter shortDateTime, @NotNull DateTimeFormatter fullDateTime) {
        this(id, name, Locale.getDefault(), ids, authors, data, shortDateTime, fullDateTime);
    }

    /**
     * Creates a new Adventure language.
     *
     * @param id            Language ID, should be non-null and unique
     * @param name          Language display name, should be non-null
     * @param locale        Language locale, should be non-null
     * @param ids           Language identification IDs, should be non-null and should not contain null elements
     * @param authors       Language authors, should be non-null and should not contain null elements
     * @param data          Language keys mapped to lists of lines, should be not null
     * @param shortDateTime Short date-time formatter
     * @param fullDateTime  Full (precise) date-time formatter
     */
    public AdvLanguage(@NotNull String id, @NotNull String name, @NotNull Locale locale, @NotNull Set<String> ids,
                       @NotNull List<String> authors, @NotNull Map<String, List<String>> data,
                       @NotNull DateTimeFormatter shortDateTime, @NotNull DateTimeFormatter fullDateTime) {
        super(id, name, locale, ids, authors, data, shortDateTime, fullDateTime);

        // Create caches.
        int capacity = (int) Math.ceil(data.size() / 0.75d);
        this.componentsCache = new ConcurrentHashMap<>(capacity);
        this.componentCache = new ConcurrentHashMap<>(capacity);
    }

    /**
     * Gets localized components.
     *
     * @param key Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public final List<Component> components(@NotNull String key) {
        return this.componentsCache.computeIfAbsent(key, k -> {
            // Extract the lines.
            List<String> lines = this.data.get(key);

            // Flush no lines.
            if (lines == null || lines.isEmpty()) {
                return List.copyOf(SRB.platform().missingKey(this, key).stream()
                        .map(line -> Component.text(line.intern()).compact())
                        .toList());
            }

            // Flush to cache.
            return lines.stream().map(line -> Component.text(line).compact()).toList();
        });
    }

    /**
     * Gets a localized component.
     *
     * @param key Localization key
     * @return Localized text component, {@code key} text component if not found
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     */
    @CheckReturnValue
    @NotNull
    public final Component component(@NotNull String key) {
        return this.componentCache.computeIfAbsent(key, k -> {
            // Extract the lines.
            List<String> lines = this.data.get(key);

            // Flush no lines.
            if (lines == null || lines.isEmpty()) {
                return Component.text(String.join("\n", SRB.platform().missingKey(this, key).stream()
                                .map(String::intern)
                                .toList()).intern())
                        .compact();
            }

            // Flush to cache.
            return Component.text(String.join("\n", lines).intern()).compact();
        });
    }

    /**
     * Tries to pre-fill all caches.
     *
     * @apiNote You are not required to call this method, though calling it might improve performance of {@link #line(String)}, {@link #lines(String)}, {@link #component(String)}, {@link #components(String)}
     */
    @Override
    public void preCache() {
        // Call super.
        super.preCache();

        // Iterate over every key.
        for (String key : this.data.keySet()) {
            // Get the components and single components.
            List<Component> ignoredComponents = this.components(key);
            Component ignoredComponent = this.component(key);
        }
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        AdvLanguage language = (AdvLanguage) obj;
        return this.id.equals(language.id) && this.name.equals(language.name) && this.locale.equals(language.locale) &&
                this.ids.equals(language.ids) && this.authors.equals(language.authors) &&
                this.shortDateTime.equals(language.shortDateTime) && this.fullDateTime.equals(language.fullDateTime) &&
                this.data.equals(language.data);
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.locale, this.ids, this.authors, this.data, this.shortDateTime, this.fullDateTime, 0x4E);
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "AdvLanguage{" +
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
                ", componentsCache=" + this.componentsCache +
                ", componentCache=" + this.componentCache +
                '}';
    }

    /**
     * Gets the language by its ID via {@link SRBPlatform#language(String)}, falling back to default language if not found.
     *
     * @param id Target ID
     * @return Language by ID, {@link #ofDefaultAdv()} if {@code id} is null or language not found
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@code AdvLanguage}
     * @see SRBPlatform#language(String)
     * @see #ofDefaultAdv()
     */
    @CheckReturnValue
    @NotNull
    public static AdvLanguage ofIdAdv(@Nullable String id) {
        if (id == null) return ofDefaultAdv();
        Language lang = Objects.requireNonNullElseGet(SRB.platform().language(id), Language::ofDefault);
        if (lang instanceof AdvLanguage advLang) return advLang;
        throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
    }

    /**
     * Gets the language via {@link SRBPlatform#of(Object)}, falling back to default language if not found or receiver not supported.
     *
     * @param receiver Target receiver
     * @return Language from receiver, {@link #ofDefaultAdv()} if language not found or receiver not supported
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@code AdvLanguage} or if receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see SRBPlatform#of(Object)
     * @see #ofIdAdv(String)
     */
    @CheckReturnValue
    @NotNull
    public static AdvLanguage ofReceiverAdv(@Nullable Object receiver) {
        Language lang = Objects.requireNonNullElseGet(SRB.platform().of(receiver), Language::ofDefault);
        if (lang instanceof AdvLanguage advLang) return advLang;
        throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
    }

    /**
     * Gets the {@link SRBPlatform#defaultLanguage()}.
     *
     * @return Default language
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@code AdvLanguage}
     * @see SRBPlatform#defaultLanguage()
     */
    @CheckReturnValue
    @NotNull
    public static AdvLanguage ofDefaultAdv() {
        Language lang = SRB.platform().defaultLanguage();
        if (lang instanceof AdvLanguage advLang) return advLang;
        throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
    }

    /**
     * Gets localized components from {@link #ofIdAdv(String)} language.
     *
     * @param id  Target language ID
     * @param key Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@code AdvLanguage}
     * @see #ofIdAdv(String)
     * @see #components(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<Component> componentsOfId(@Nullable String id, @NotNull String key) {
        return ofIdAdv(id).components(key);
    }

    /**
     * Gets a localized text component from {@link #ofIdAdv(String)} language.
     *
     * @param id  Target language ID
     * @param key Localization key
     * @return Localized text component, {@code key} text component if not found
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@code AdvLanguage}
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     * @see #ofIdAdv(String)
     * @see #component(String)
     */
    @CheckReturnValue
    @NotNull
    public static Component componentOfId(@Nullable String id, @NotNull String key) {
        return ofIdAdv(id).component(key);
    }

    /**
     * Gets localized components from {@link #ofReceiverAdv(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     * @throws IllegalArgumentException If the language of that receiver is not an instance of {@code AdvLanguage} or if receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiverAdv(Object)
     * @see #components(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<Component> componentsOfReceiver(@Nullable Object receiver, @NotNull String key) {
        return ofReceiverAdv(receiver).components(key);
    }

    /**
     * Gets a localized text component from {@link #ofReceiverAdv(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized text component, {@code key} text component if not found
     * @throws IllegalArgumentException If the language of that receiver is not an instance of {@code AdvLanguage} or if receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     * @see #ofReceiverAdv(Object)
     * @see #component(String)
     */
    @CheckReturnValue
    @NotNull
    public static Component componentOfReceiver(@Nullable Object receiver, @NotNull String key) {
        return ofReceiverAdv(receiver).component(key);
    }

    /**
     * Gets localized components from {@link #ofDefaultAdv()} language.
     *
     * @param key Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     * @throws IllegalStateException If the default language is not an instance of {@code AdvLanguage}
     * @see #ofDefaultAdv()
     * @see #components(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<Component> componentsOfDefault(@NotNull String key) {
        return ofDefaultAdv().components(key);
    }

    /**
     * Gets a localized text component from {@link #ofDefaultAdv()} language.
     *
     * @param key Localization key
     * @return Localized text component, {@code key} text component if not found
     * @throws IllegalStateException If the default language is not an instance of {@code AdvLanguage}
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     * @see #ofIdAdv(String)
     * @see #component(String)
     */
    @CheckReturnValue
    @NotNull
    public static Component componentOfDefault(@NotNull String key) {
        return ofDefaultAdv().component(key);
    }

    /**
     * Recreates the target language as an Adventure language.
     *
     * @param language Target language
     * @return New adventure language, {@code language} if the language is instance of {@code AdvLanguage}
     */
    @CheckReturnValue
    @NotNull
    public static AdvLanguage asAdventure(@NotNull Language language) {
        // Return as-is if already AdvLanguage.
        if (language instanceof AdvLanguage advLang) return advLang;

        // Create a new adventure language.
        return new AdvLanguage(language.id(), language.name(), language.locale(), language.ids(), language.authors(), language.data(), language.shortDateTime(), language.fullDateTime());
    }
}
