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
    public static List<CheckResult> checkSingle(@NotNull Language base, @NotNull Collection<Language> compares) {
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
