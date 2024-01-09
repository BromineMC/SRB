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

package ru.brominemc.srb;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * SRB is the localization/translation library for BromineMC.
 *
 * @author VidTu
 */
public final class SRB {
    /**
     * Current platform.
     */
    private static SRBPlatform platform;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private SRB() {
        throw new AssertionError("No instances.");
    }

    /**
     * Gets the SRB platform.
     *
     * @return Current SRB platform
     * @throws NullPointerException If platform wasn't set via {@link #platform(SRBPlatform)}
     * @apiNote You <b>MUST</b> set a platform before using this API using {@link #platform(SRBPlatform)}
     * @see #platform(SRBPlatform)
     */
    @Contract(pure = true)
    @NotNull
    public static SRBPlatform platform() {
        Objects.requireNonNull(platform, "Language platform is not set. Use SRB.platform(SRBPlatform) to set it.");
        return platform;
    }

    /**
     * Sets the SRB platform.
     *
     * @param platform New SRB platform, {@code null} to unload the platform
     * @see #platform()
     */
    public static void platform(@Nullable SRBPlatform platform) {
        SRB.platform = platform;
    }
}