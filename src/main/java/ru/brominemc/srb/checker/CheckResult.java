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
        return extraKeys.isEmpty() && missingKeys.isEmpty();
    }

    /**
     * Checks whether extra or missing keys ARE found.
     *
     * @return Whether extra or missing keys ARE found
     */
    @Contract(pure = true)
    public boolean dirty() {
        return !extraKeys.isEmpty() || !missingKeys.isEmpty();
    }

    /**
     * Returns the clean check result for equal checking language.
     *
     * @param language Target language
     * @return Clean check result
     * @apiNote Internal use only. Used when {@link LanguageChecker#checkSingle(Language, Language)} parameters are equal.
     */
    @ApiStatus.Internal
    public static CheckResult forSame(@NotNull Language language) {
        return new CheckResult(language, language, Set.of(), Set.of());
    }
}
