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

package ru.brominemc.srb.checker;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.brominemc.srb.Language;

import java.util.Set;

/**
 * Result of checking via {@link LanguageChecker}.
 *
 * @param base        Base language
 * @param compared    Compared language
 * @param extraKeys   An unmodifiable set of extra language keys
 * @param missingKeys An unmodifiable set of missing language keys
 * @author VidTu
 */
public record CheckResult(@NotNull Language base, @NotNull Language compared,
                          @NotNull @Unmodifiable Set<String> extraKeys,
                          @NotNull @Unmodifiable Set<String> missingKeys) {
    /**
     * Creates a new check result.
     *
     * @param base        Base language
     * @param compared    Compared language
     * @param extraKeys   Set of extra language keys
     * @param missingKeys Set of missing language keys
     */
    public CheckResult(@NotNull Language base, @NotNull Language compared,
                       @NotNull Set<String> extraKeys, @NotNull Set<String> missingKeys) {
        this.base = base;
        this.compared = compared;
        this.extraKeys = Set.copyOf(extraKeys);
        this.missingKeys = Set.copyOf(missingKeys);
    }

    /**
     * Checks whether there are NO extra or missing keys.
     *
     * @return Whether there are NO extra or missing keys
     */
    @Contract(pure = true)
    public boolean clean() {
        return this.extraKeys.isEmpty() && this.missingKeys.isEmpty();
    }

    /**
     * Checks whether extra or missing keys ARE found.
     *
     * @return Whether extra or missing keys ARE found
     */
    @Contract(pure = true)
    public boolean dirty() {
        return !this.extraKeys.isEmpty() || !this.missingKeys.isEmpty();
    }

    /**
     * Returns the clean check result for equal checking language.
     *
     * @param language Target language
     * @return Clean check result
     * @apiNote Internal use only
     */
    @ApiStatus.Internal
    static CheckResult same(@NotNull Language language) {
        return new CheckResult(language, language, Set.of(), Set.of());
    }
}
