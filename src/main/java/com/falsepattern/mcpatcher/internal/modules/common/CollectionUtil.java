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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class CollectionUtil {
    private CollectionUtil() {
        throw new UnsupportedOperationException();
    }

    public static <K> @NotNull @UnmodifiableView ObjectList<K> lockList(@Nullable ObjectList<K> list) {
        if (list instanceof ObjectLists.EmptyList) {
            return list;
        }
        if (list instanceof ObjectLists.UnmodifiableList) {
            return list;
        }
        if (list instanceof ObjectLists.Singleton) {
            return list;
        }

        if (list == null || list.isEmpty()) {
            return ObjectLists.emptyList();
        }
        if (list.size() == 1) {
            return ObjectLists.singleton(list.get(0));
        }

        return ObjectLists.unmodifiable(list);
    }

    public static <K> @NotNull @UnmodifiableView ObjectSet<K> lockSet(@Nullable ObjectSet<K> set) {
        if (set instanceof ObjectSets.EmptySet) {
            return set;
        }
        if (set instanceof ObjectSets.UnmodifiableSet) {
            return set;
        }
        if (set instanceof ObjectSets.Singleton) {
            return set;
        }

        if (set == null || set.isEmpty()) {
            return ObjectSets.emptySet();
        }
        if (set.size() == 1) {
            return ObjectSets.singleton(set.iterator()
                                           .next());
        }

        return ObjectSets.unmodifiable(set);
    }

    public static <K, V> @NotNull @UnmodifiableView Object2ObjectMap<K, V> lockMap(@Nullable Object2ObjectMap<K, V> map) {
        if (map instanceof Object2ObjectMaps.EmptyMap) {
            return map;
        }
        if (map instanceof Object2ObjectMaps.UnmodifiableMap) {
            return map;
        }
        if (map instanceof Object2ObjectMaps.Singleton) {
            return map;
        }

        if (map == null || map.isEmpty()) {
            return Object2ObjectMaps.emptyMap();
        }
        if (map.size() == 1) {
            val entry = map.entrySet()
                           .iterator()
                           .next();
            return Object2ObjectMaps.singleton(entry.getKey(), entry.getValue());
        }

        return Object2ObjectMaps.unmodifiable(map);
    }
}
