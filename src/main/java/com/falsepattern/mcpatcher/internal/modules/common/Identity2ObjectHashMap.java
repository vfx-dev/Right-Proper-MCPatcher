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

package com.falsepattern.mcpatcher.internal.modules.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public class Identity2ObjectHashMap<K, V> extends Object2ObjectOpenCustomHashMap<K, V> {
    public static final int DEFAULT_EXPECTED = 128;
    public static final float DEFAULT_LOAD_FACTOR = 0.1F;

    public Identity2ObjectHashMap() {
        super(DEFAULT_EXPECTED, DEFAULT_LOAD_FACTOR, new IdentityStrategy<>());
    }

    public Identity2ObjectHashMap(int expected, float f) {
        super(expected, f, new IdentityStrategy<>());
    }

    public Identity2ObjectHashMap(int expected, float f, Strategy<? super K> strategy) {
        super(expected, f, strategy);
    }

    public static class IdentityStrategy<K> implements Strategy<K> {
        @Override
        public int hashCode(K o) {
            return System.identityHashCode(o);
        }

        @Override
        public boolean equals(K a, K b) {
            return a == b;
        }
    }
}
