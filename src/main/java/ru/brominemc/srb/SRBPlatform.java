package ru.brominemc.srb;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Platform for handling SRB.
 *
 * @author VidTu
 * @see SRB#platform()
 * @see SRB#platform(SRBPlatform)
 */
public interface SRBPlatform {
    /**
     * Gets the logger for the SRB.
     *
     * @return Logger
     * @implNote Currently used for logging missing keys
     */
    @CheckReturnValue
    @NotNull
    System.Logger logger();

    /**
     * Gets the default platform language.
     *
     * @return Default platform language
     */
    @CheckReturnValue
    @NotNull
    Language defaultLanguage();

    /**
     * Gets the language by its ID.
     *
     * @param id Target ID
     * @return Language by ID, {@code null} if not found
     */
    @CheckReturnValue
    @Nullable
    Language language(@NotNull String id);

    /**
     * Gets the language from the target receiver.
     *
     * @param receiver Target receiver
     * @return Language from receiver, {@code null} if not found or receiver is not supported
     * @throws IllegalArgumentException If receiver is not supported and the platform chose not to return {@code null}
     * @throws NullPointerException     If receiver is null and the platform chose not to return {@code null}
     * @apiNote What is "receiver" is up to platform to decide. It may be a bukkit Player object, may be an Adventure Audience with some pointer, or may be ANY object whatsoever - It's up to platform to decide. This method may also choose to either return {@code null} for unknown {@code receiver} and allow other implementations to handle it or throw {@link IllegalArgumentException} or {@link NullPointerException} to fail.
     */
    @CheckReturnValue
    @Nullable
    Language of(@Nullable Object receiver);
}
