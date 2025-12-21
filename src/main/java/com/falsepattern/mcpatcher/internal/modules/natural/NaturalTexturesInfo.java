/*
 * Right Proper MCPatcher
 *
 * Copyright (C) 2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.mcpatcher.internal.modules.natural;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.mcpatcher.internal.modules.natural.NaturalTexturesEngine.LOG;

@Getter
@Accessors(fluent = true,
           chain = false)
public class NaturalTexturesInfo {
    private static final UVRotation DEFAULT_ROTATION = UVRotation.None;
    private static final boolean DEFAULT_FLIP_HORIZONTALLY = false;

    public enum UVRotation {
        None,
        Two, // 180 degrees rotations
        Four, // 90 degrees rotations
    }

    private final UVRotation rotation;
    private final boolean flipHorizontally;

    public NaturalTexturesInfo() {
        rotation = DEFAULT_ROTATION;
        flipHorizontally = DEFAULT_FLIP_HORIZONTALLY;
    }

    public NaturalTexturesInfo(@Nullable String propString) {
        if(propString == null || propString.isEmpty()) {
            rotation = DEFAULT_ROTATION;
            flipHorizontally = DEFAULT_FLIP_HORIZONTALLY;
            return;
        }

        propString = propString.trim().toUpperCase();

        switch (propString) {
            case "0":
                rotation = UVRotation.None;
                flipHorizontally = false;
                break;

            case "2":
                rotation = UVRotation.Two;
                flipHorizontally = DEFAULT_FLIP_HORIZONTALLY;
                break;

            case "4":
                rotation = UVRotation.Four;
                flipHorizontally = DEFAULT_FLIP_HORIZONTALLY;
                break;

            case "F":
                rotation = DEFAULT_ROTATION;
                flipHorizontally = true;
                break;

            case "2F":
            case "F2":
                rotation = UVRotation.Two;
                flipHorizontally = true;
                break;

            case "4F":
            case "F4":
                rotation = UVRotation.Four;
                flipHorizontally = true;
                break;

            default:
                LOG.warn("Unknown pattern in natural.properties: [pattern={}]", propString);
                rotation = DEFAULT_ROTATION;
                flipHorizontally = DEFAULT_FLIP_HORIZONTALLY;
                break;
        }
    }
}

