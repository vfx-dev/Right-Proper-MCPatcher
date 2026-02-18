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

import com.falsepattern.mcpatcher.internal.modules.common.Side;

public enum Symmetry {
    None,
    Opposite,
    All,
    Unknown;

    public Side apply(Side s) {
        switch (this) {
            case None:
                return s;
            case Opposite:
                switch (s) {
                    case XNeg:
                    case XPos:
                        return Side.XNeg;
                    case YNeg:
                    case YPos:
                        return Side.YNeg;
                    case ZNeg:
                    case ZPos:
                        return Side.ZNeg;
                }
            default:
                return Side.YNeg;
        }
    }
}
