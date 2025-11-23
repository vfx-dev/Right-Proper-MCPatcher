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

package com.falsepattern.mcpatcher.internal.modules.cit;

import com.falsepattern.lib.util.MathUtil;
import com.falsepattern.mcpatcher.internal.modules.common.IntRange;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import net.minecraft.item.ItemStack;

@Getter
@Accessors(fluent = true,
           chain = false)
public final class DamageRangeList extends IntRange.List {
    private final boolean isPercent;

    public DamageRangeList(IntRange.List ranges, boolean isPercent) {
        super(ranges);
        this.isPercent = isPercent;
    }

    public boolean isInRange(ItemStack itemStack, int damageMask) {
        if (!isPercent) {
            return isInRange(itemStack.getItemDamage() & damageMask);
        }

        val maxDamage = itemStack.getMaxDamage();
        if (maxDamage == 0) {
            return false;
        }
        val percent = (100 * itemStack.getItemDamage()) / maxDamage;
        return isInRange(MathUtil.clamp(percent, 0, 100));
    }
}
