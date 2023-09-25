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

package ru.brominemc.srb.adventure;

import com.google.errorprone.annotations.CheckReturnValue;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.brominemc.srb.Language;
import ru.brominemc.srb.SRB;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Language instance with support for Kyori Adventure API.
 *
 * @author VidTu
 */
public class AdvLanguage extends Language {
    // Caches
    protected final Map<String, List<Component>> componentsCache;
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
     */
    public AdvLanguage(@NotNull String id, @NotNull String name, @NotNull Set<String> ids,
                       @NotNull List<String> authors, @NotNull Map<String, List<String>> data,
                       @NotNull DateTimeFormatter shortDateTime, @NotNull DateTimeFormatter fullDateTime) {
        super(id, name, ids, authors, data, shortDateTime, fullDateTime);

        // Create caches
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
        return componentsCache.computeIfAbsent(key, k -> {
            List<String> lines = data.get(key);
            if (lines == null || lines.isEmpty()) {
                SRB.platform().logger().log(System.Logger.Level.WARNING, "Unable to get key from lang %s: %s", id, key);
                return List.of(Component.text(key.intern()));
            }
            return lines.stream().map(line -> (Component) Component.text(line)).toList();
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
        return componentCache.computeIfAbsent(key, k -> {
            List<String> lines = data.get(key);
            if (lines == null || lines.isEmpty()) {
                SRB.platform().logger().log(System.Logger.Level.WARNING, "Unable to get key from lang %s: %s", id, key);
                return Component.text(key.intern());
            }
            return Component.text(String.join("\n", lines).intern());
        });
    }

    /**
     * Tries to pre-fill all caches.
     *
     * @apiNote You are not required to call this method, though calling it might improve performance of {@link #line(String)}, {@link #lines(String)}, {@link #component(String)}, {@link #components(String)}
     */
    @Override
    public void preCache() {
        super.preCache();
        for (String key : data.keySet()) {
            List<Component> ignoredComponents = components(key);
            Component ignoredComponent = component(key);
        }
    }

    /**
     * Gets localized components from {@link #ofId(String)} language.
     *
     * @param id  Target language ID
     * @param key Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@link AdvLanguage}
     * @see #ofId(String)
     * @see #components(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<Component> componentsOfId(@Nullable String id, @NotNull String key) {
        Language lang = ofId(id);
        if (!(lang instanceof AdvLanguage advLang)) {
            throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
        }
        return advLang.components(key);
    }

    /**
     * Gets a localized text component from {@link #ofId(String)} language.
     *
     * @param id  Target language ID
     * @param key Localization key
     * @return Localized text component, {@code key} text component if not found
     * @throws IllegalArgumentException If the language by that ID is not an instance of {@link AdvLanguage}
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     * @see #ofId(String)
     * @see #component(String)
     */
    @CheckReturnValue
    @NotNull
    public static Component componentOfId(@Nullable String id, @NotNull String key) {
        Language lang = ofId(id);
        if (!(lang instanceof AdvLanguage advLang)) {
            throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
        }
        return advLang.component(key);
    }

    /**
     * Gets localized components from {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     * @throws IllegalArgumentException If the language of that receiver is not an instance of {@link AdvLanguage} or if receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @see #ofReceiver(Object)
     * @see #components(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<Component> componentsOfReceiver(@Nullable Object receiver, @NotNull String key) {
        Language lang = ofReceiver(receiver);
        if (!(lang instanceof AdvLanguage advLang)) {
            throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
        }
        return advLang.components(key);
    }

    /**
     * Gets a localized text component from {@link #ofReceiver(Object)} language.
     *
     * @param receiver Target receiver
     * @param key      Localization key
     * @return Localized text component, {@code key} text component if not found
     * @throws IllegalArgumentException If the language of that receiver is not an instance of {@link AdvLanguage} or if receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is {@code null} and the platform chose not to return {@code null}
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     * @see #ofReceiver(Object)
     * @see #component(String)
     */
    @CheckReturnValue
    @NotNull
    public static Component componentOfReceiver(@Nullable Object receiver, @NotNull String key) {
        Language lang = ofReceiver(receiver);
        if (!(lang instanceof AdvLanguage advLang)) {
            throw new IllegalArgumentException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
        }
        return advLang.component(key);
    }

    /**
     * Gets localized components from {@link #ofDefault()} language.
     *
     * @param key Localization key
     * @return Localized text components, singleton {@code key} text component if not found
     * @throws IllegalStateException If the default language is not an instance of {@link AdvLanguage}
     * @see #ofDefault()
     * @see #components(String)
     */
    @CheckReturnValue
    @NotNull
    @Unmodifiable
    public static List<Component> componentsOfDefault(@NotNull String key) {
        Language lang = ofDefault();
        if (!(lang instanceof AdvLanguage advLang)) {
            throw new IllegalStateException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
        }
        return advLang.components(key);
    }

    /**
     * Gets a localized text component from {@link #ofDefault()} language.
     *
     * @param key Localization key
     * @return Localized text component, {@code key} text component if not found
     * @throws IllegalStateException If the default language is not an instance of {@link AdvLanguage}
     * @apiNote This method will join components with LF ({@code \n}) if multiple are found
     * @see #ofId(String)
     * @see #component(String)
     */
    @CheckReturnValue
    @NotNull
    public static Component componentOfDefault(@NotNull String key) {
        Language lang = ofDefault();
        if (!(lang instanceof AdvLanguage advLang)) {
            throw new IllegalStateException("Target language is not AdventureLanguage (" + lang.getClass() + "): " + lang);
        }
        return advLang.component(key);
    }

    /**
     * Recreates the target language as an Adventure language.
     *
     * @param language Target language
     * @return New adventure language, {@code language} if the language is instance of {@link AdvLanguage}
     */
    @CheckReturnValue
    @NotNull
    public static AdvLanguage asAdventure(@NotNull Language language) {
        if (language instanceof AdvLanguage advLang) return advLang;
        return new AdvLanguage(language.id(), language.name(), language.ids(), language.authors(), language.data(), language.shortDateTime(), language.fullDateTime());
    }
}
