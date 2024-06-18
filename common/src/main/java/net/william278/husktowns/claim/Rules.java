/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.claim;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.william278.cloplib.operation.OperationType;
import net.william278.husktowns.config.Flags;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Claim rules, defining what players can do in a claim
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Rules {

    @Expose
    private Map<String, Boolean> flags;

    @Expose(deserialize = false, serialize = false)
    private Map<Flag, Boolean> calculatedFlags;

    private Rules(@NotNull Map<String, Boolean> flags) {
        this.flags = flags;
    }

    @NotNull
    private static Map<Flag, Boolean> getMapped(@NotNull Map<String, Boolean> flags, @NotNull Flags flagConfig) {
        Map<Flag, Boolean> mappedFlags = new EnumMap<>(Flag.class);
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            Optional<Flag> flag = flagConfig.getFlag(entry.getKey());
            flag.ifPresent(f -> mappedFlags.put(f, entry.getValue()));
        }
        return mappedFlags;
    }

    @NotNull
    public static Rules of(@NotNull Map<Flag, Boolean> rules) {
        Map<String, Boolean> flags = new EnumMap<>(String.class);
        for (Map.Entry<Flag, Boolean> entry : rules.entrySet()) {
            flags.put(entry.getKey().getName(), entry.getValue());
        }
        return new Rules(flags);
    }

    @NotNull
    public static Rules from(@NotNull Map<String, Boolean> flags) {
        return new Rules(flags);
    }

    @NotNull
    public Map<Flag, Boolean> getCalculatedFlags(@NotNull Flags flagConfig) {
        if (calculatedFlags == null) {
            calculatedFlags = getMapped(flags, flagConfig);
        }
        return calculatedFlags;
    }

    public boolean hasFlagSet(@NotNull Flag flag) {
        return flags.containsKey(flag.getName());
    }

    public void setFlag(@NotNull Flag flag, boolean value) {
        flags.put(flag.getName(), value);
        if (calculatedFlags != null) {
            calculatedFlags.put(flag, value);
        }
    }

    public boolean cancelOperation(@NotNull OperationType type, @NotNull Flags flagConfig) {
        for (Map.Entry<Flag, Boolean> entry : getCalculatedFlags(flagConfig).entrySet()) {
            if (entry.getValue() && entry.getKey().isOperationAllowed(type)) {
                return false;
            }
        }
        return true;
    }

}
