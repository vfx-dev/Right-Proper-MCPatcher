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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.val;
import org.jetbrains.annotations.NotNull;

public class WeightedRandom {
    private final @NotNull IntList weights;
    private final @NotNull IntList sumWeights;
    private final int sumAllWeights;

    public WeightedRandom(@NotNull IntList weights) {
        this(weights, weights.size());
    }

    public WeightedRandom(@NotNull IntList weights, int size) {
        weights = new IntArrayList(weights);
        if (size < weights.size()) {
            weights.size(size);
        } else {
            int avg = getAverage(weights);
            for (int i = weights.size(); i < size; i++) {
                weights.add(avg);
            }
        }
        this.weights = weights;
        this.sumWeights = new IntArrayList(this.weights.size());
        int sum = 0;

        for (int i = 0; i < this.weights.size(); ++i) {
            sum += this.weights.getInt(i);
            this.sumWeights.add(sum);
        }
        if (sum <= 0) {
            CommonParser.LOG.warn("Invalid sum of all weights: {}", sum);
            sum = 1;
        }
        this.sumAllWeights = sum;
    }

    private static int getAverage(IntList vals) {
        if (vals.isEmpty()) {
            return 0;
        }
        int sum = 0;

        val len = vals.size();
        for (int i = 0; i < len; ++i) {
            int val = vals.getInt(i);
            sum += val;
        }

        return sum / len;
    }

    public int size() {
        return weights.size();
    }

    public int getIndex(int rand) {
        int index = 0;
        int randWeight = rand % sumAllWeights;
        int len = sumWeights.size();
        for (int i = 0; i < len; i++) {
            if (randWeight < sumWeights.getInt(i)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public @NotNull WeightedRandom resize(int size) {
        return new WeightedRandom(weights, size);
    }
}
