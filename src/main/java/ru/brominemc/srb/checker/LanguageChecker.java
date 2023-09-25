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

package ru.brominemc.srb.checker;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.brominemc.srb.Language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for language differences.
 *
 * @author VidTu
 */
public final class LanguageChecker {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private LanguageChecker() {
        throw new AssertionError("No instances.");
    }

    /**
     * Checks for differences between languages.
     *
     * @param base     Target base language
     * @param compares Target compare languages
     * @return Check results
     */
    @Contract(pure = true)
    @NotNull
    public static List<CheckResult> checkMulti(@NotNull Language base, @NotNull Collection<Language> compares) {
        List<CheckResult> results = new ArrayList<>(compares.size());
        for (Language compare : compares) {
            results.add(checkSingle(base, compare));
        }
        return results;
    }

    /**
     * Checks for differences between languages.
     *
     * @param base    Target base language
     * @param compare Target compare language
     * @return Check results
     */
    @Contract(pure = true)
    @NotNull
    public static CheckResult checkSingle(@NotNull Language base, @NotNull Language compare) {
        if (base == compare) return CheckResult.forSame(base);
        Set<String> baseKeys = base.data().keySet();
        Set<String> compareKeys = compare.data().keySet();

        Set<String> extraKeys = new HashSet<>(compareKeys);
        extraKeys.removeAll(baseKeys);

        Set<String> missingKeys = new HashSet<>(baseKeys);
        missingKeys.removeAll(compareKeys);

        return new CheckResult(base, compare, extraKeys, missingKeys);
    }
}
