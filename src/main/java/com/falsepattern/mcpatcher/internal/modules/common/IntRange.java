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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class IntRange {
    public final int min;
    public final int max;

    public boolean isInRange(int v) {
        return v >= min && v <= max;
    }

    public static class List extends ObjectArrayList<IntRange> {
        public List(int capacity) {
            super(capacity);
        }

        public List() {
            super();
        }

        public List(Collection<? extends IntRange> c) {
            super(c);
        }

        public List(ObjectCollection<? extends IntRange> c) {
            super(c);
        }

        public List(ObjectList<? extends IntRange> l) {
            super(l);
        }

        public List(IntRange[] a) {
            super(a);
        }

        public List(IntRange[] a, int offset, int length) {
            super(a, offset, length);
        }

        public List(Iterator<? extends IntRange> i) {
            super(i);
        }

        public List(ObjectIterator<? extends IntRange> i) {
            super(i);
        }

        public boolean isInRange(int v) {
            for (val range : this) {
                if (range.isInRange(v)) {
                    return true;
                }
            }
            return false;
        }
    }
}
