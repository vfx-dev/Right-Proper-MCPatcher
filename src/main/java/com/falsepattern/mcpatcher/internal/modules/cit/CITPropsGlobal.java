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

import com.falsepattern.mcpatcher.internal.modules.common.CommonParser;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

import static com.falsepattern.mcpatcher.internal.modules.cit.CITEngine.LOG;

/**
 * @see <a href="https://bitbucket.org/prupe/mcpatcher/src/master/doc/cit.properties">MCPatcher cit.properties</a>
 */
@Getter
@Accessors(fluent = true,
           chain = false)
public final class CITPropsGlobal {
    private static final Method DEFAULT_METHOD = Method.AVERAGE;
    private static final int DEFAULT_CAP = 99;
    private static final float DEFAULT_FADE = 0.5F;
    private static final boolean DEFAULT_USE_GLINT = true;

    private final Method method;
    private final int cap;
    private final float fade;
    private final boolean useGlint;

    public CITPropsGlobal() {
        this.method = DEFAULT_METHOD;
        this.cap = DEFAULT_CAP;
        this.fade = DEFAULT_FADE;
        this.useGlint = DEFAULT_USE_GLINT;
    }

    public CITPropsGlobal(Properties props) {
        this.method = Method.parse(props.getProperty("method"), DEFAULT_METHOD);
        this.cap = Math.max(CommonParser.parseInt(props.getProperty("cap"), DEFAULT_CAP), 0);
        this.fade = Math.max(CommonParser.parseFloat(props.getProperty("fade"), DEFAULT_FADE), 0F);
        this.useGlint = CommonParser.parseBoolean("glint", DEFAULT_USE_GLINT);
    }

    public enum Method {
        AVERAGE,
        LAYERED,
        CYCLE,
        ;

        public static Method parse(@Nullable String value, Method def) {
            if (value == null) {
                return def;
            }
            switch (value) {
                case "average":
                    return AVERAGE;
                case "layered":
                    return LAYERED;
                case "cycle":
                    return CYCLE;
                default: {
                    LOG.warn("Unknown method in cit.properties: [method={}]", value);
                    return def;
                }
            }
        }
    }
}
