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

package com.falsepattern.mcpatcher.internal.modules.ctm;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import net.minecraftforge.common.util.ForgeDirection;

import static com.falsepattern.mcpatcher.internal.modules.ctm.Axis.X;
import static com.falsepattern.mcpatcher.internal.modules.ctm.Axis.Y;
import static com.falsepattern.mcpatcher.internal.modules.ctm.Axis.Z;


/**
 * @implSpec Match order with: {@link ForgeDirection}
 */
@RequiredArgsConstructor
public enum Side {
    YNeg(Y),
    YPos(Y),
    ZNeg(Z),
    ZPos(Z),
    XNeg(X),
    XPos(X);

    // @formatter:off
    public static final Side Bottom = YNeg;
    public static final Side Top    = YPos;
    public static final Side North  = ZNeg;
    public static final Side South  = ZPos;
    public static final Side West   = XNeg;
    public static final Side East   = XPos;
    // @formatter:on

    public static final int MASK_SIDES = ZNeg.mask | ZPos.mask | XNeg.mask | XPos.mask;
    public static final int MASK_ALL = MASK_SIDES | YNeg.mask | YPos.mask;
    public static final int MASK_UNKNOWN = 0b10000000;

    private static final Side[] BY_INDEX = values();

    public final int mask = 1 << ordinal();
    public final Axis axis;

    public static boolean matches(int mask, int value) {
        return (value & mask) == mask;
    }

    public static @Nullable Side fromMCDirection(int direction) {
        if (direction < 0 || direction >= BY_INDEX.length) {
            return null;
        }
        return BY_INDEX[direction];
    }

    public boolean matches(int value) {
        return matches(mask, value);
    }
}
