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

import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

abstract public class NBTRule {
    public static final String NBT_RULE_PREFIX = "nbt.";
    public static final String NBT_RULE_SEPARATOR = ".";
    public static final String NBT_RULE_WILDCARD = "*";
    public static final String NBT_REGEX_PREFIX = "regex:";
    public static final String NBT_IREGEX_PREFIX = "iregex:";
    public static final String NBT_GLOB_PREFIX = "pattern:";
    public static final String NBT_IGLOB_PREFIX = "ipattern:";

    private final String[] tagName;
    private final Integer[] tagIndex;

    public static @Nullable NBTRule create(@Nullable String tag, @Nullable String value) {
        if (tag == null || value == null || !tag.startsWith(NBT_RULE_PREFIX)) {
            return null;
        }
        try {
            tag = tag.substring(NBT_RULE_PREFIX.length());
            if (value.startsWith(NBT_REGEX_PREFIX)) {
                return new Regex(tag, value.substring(NBT_REGEX_PREFIX.length()), true);
            } else if (value.startsWith(NBT_IREGEX_PREFIX)) {
                return new Regex(tag, value.substring(NBT_IREGEX_PREFIX.length()), false);
            } else if (value.startsWith(NBT_GLOB_PREFIX)) {
                return new Glob(tag, value.substring(NBT_GLOB_PREFIX.length()), true);
            } else if (value.startsWith(NBT_IGLOB_PREFIX)) {
                return new Glob(tag, value.substring(NBT_IGLOB_PREFIX.length()), false);
            } else {
                return new Exact(tag, value);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    protected NBTRule(String tag, String value) {
        tagName = tag.split(Pattern.quote(NBT_RULE_SEPARATOR));
        tagIndex = new Integer[tagName.length];
        for (int i = 0; i < tagName.length; i++) {
            if (NBT_RULE_WILDCARD.equals(tagName[i])) {
                tagName[i] = null;
                tagIndex[i] = null;
            } else {
                try {
                    tagIndex[i] = Integer.valueOf(tagName[i]);
                } catch (NumberFormatException e) {
                    tagIndex[i] = -1;
                }
            }
        }
    }

    public final boolean match(NBTTagCompound nbt) {
        return nbt != null && match(nbt, 0);
    }

    private boolean match(NBTTagCompound nbt, int index) {
        if (index >= tagName.length) {
            return false;
        } else if (tagName[index] == null) {
            //noinspection unchecked
            val tags = (Collection<NBTBase>) nbt.tagMap.values();
            for (NBTBase nbtBase : tags) {
                if (match1(nbtBase, index + 1)) {
                    return true;
                }
            }
            return false;
        } else {
            NBTBase nbtBase = nbt.getTag(tagName[index]);
            return match1(nbtBase, index + 1);
        }
    }

    private boolean match(NBTTagList nbt, int index) {
        if (index >= tagIndex.length) {
            return false;
        }

        //noinspection unchecked
        val tags = (List<NBTBase>) nbt.tagList;

        if (tagIndex[index] != null) {
            int tagNum = tagIndex[index];
            return tagNum >= 0 && tagNum < nbt.tagCount() && match1(tags.get(tagNum), index + 1);
        }

        for (int i = 0; i < nbt.tagCount(); i++) {
            if (match1(tags.get(i), index + 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean match1(NBTBase nbt, int index) {
        if (nbt == null) {
            return false;
        } else if (nbt instanceof NBTTagCompound) {
            return match((NBTTagCompound) nbt, index);
        } else if (nbt instanceof NBTTagList) {
            return match((NBTTagList) nbt, index);
        } else if (index < tagName.length) {
            return false;
        } else if (nbt instanceof NBTTagString) {
            return match((NBTTagString) nbt);
        } else if (nbt instanceof NBTTagInt) {
            return match((NBTTagInt) nbt);
        } else if (nbt instanceof NBTTagDouble) {
            return match((NBTTagDouble) nbt);
        } else if (nbt instanceof NBTTagFloat) {
            return match((NBTTagFloat) nbt);
        } else if (nbt instanceof NBTTagLong) {
            return match((NBTTagLong) nbt);
        } else if (nbt instanceof NBTTagShort) {
            return match((NBTTagShort) nbt);
        } else if (nbt instanceof NBTTagByte) {
            return match((NBTTagByte) nbt);
        } else {
            return false;
        }
    }

    protected boolean match(NBTTagByte nbt) {
        return false;
    }

    protected boolean match(NBTTagDouble nbt) {
        return false;
    }

    protected boolean match(NBTTagFloat nbt) {
        return false;
    }

    protected boolean match(NBTTagInt nbt) {
        return false;
    }

    protected boolean match(NBTTagLong nbt) {
        return false;
    }

    protected boolean match(NBTTagShort nbt) {
        return false;
    }

    protected boolean match(NBTTagString nbt) {
        return false;
    }

    private static final class Exact extends NBTRule {

        private final Byte byteValue;
        private final Double doubleValue;
        private final Float floatValue;
        private final Integer integerValue;
        private final Long longValue;
        private final Short shortValue;
        private final String stringValue;

        Exact(String tag, String value) {
            super(tag, value);
            this.stringValue = value;

            Double tempDouble;
            try {
                tempDouble = Double.valueOf(value);
            } catch (RuntimeException e) {
                tempDouble = null;
            }
            if (tempDouble != null) {
                this.doubleValue = tempDouble;
                this.floatValue = tempDouble.floatValue();
            } else {
                this.doubleValue = null;
                this.floatValue = null;
            }

            Long tempLong;
            try {
                tempLong = Long.valueOf(value);
            } catch (RuntimeException e) {
                tempLong = null;
            }
            if (tempLong == null) {
                this.longValue = null;
                this.byteValue = null;
                this.integerValue = null;
                this.shortValue = null;
            } else {
                this.longValue = tempLong;
                this.byteValue = tempLong.byteValue();
                this.integerValue = tempLong.intValue();
                this.shortValue = tempLong.shortValue();
            }
        }

        @Override
        protected boolean match(NBTTagByte nbt) {
            return byteValue != null && byteValue == nbt.func_150290_f();
        }

        @Override
        protected boolean match(NBTTagDouble nbt) {
            return doubleValue != null && doubleValue == nbt.func_150286_g();
        }

        @Override
        protected boolean match(NBTTagFloat nbt) {
            return floatValue != null && floatValue == nbt.func_150288_h();
        }

        @Override
        protected boolean match(NBTTagInt nbt) {
            return integerValue != null && integerValue == nbt.func_150287_d();
        }

        @Override
        protected boolean match(NBTTagLong nbt) {
            return longValue != null && longValue == nbt.func_150291_c();
        }

        @Override
        protected boolean match(NBTTagShort nbt) {
            return shortValue != null && shortValue == nbt.func_150289_e();
        }

        @Override
        protected boolean match(NBTTagString nbt) {
            return nbt.func_150285_a_()
                      .equals(stringValue);
        }
    }

    private static final class Regex extends NBTRule {

        private final Pattern pattern;

        Regex(String tag, String value, boolean caseSensitive) {
            super(tag, value);
            pattern = Pattern.compile(value, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }

        @Override
        protected boolean match(NBTTagString nbt) {
            return pattern.matcher(nbt.func_150285_a_())
                          .matches();
        }
    }

    private static final class Glob extends NBTRule {

        private static final char STAR = '*';
        private static final char SINGLE = '?';
        private static final char ESCAPE = '\\';

        private final String glob;
        private final boolean caseSensitive;

        protected Glob(String tag, String value, boolean caseSensitive) {
            super(tag, value);
            this.caseSensitive = caseSensitive;
            if (!caseSensitive) {
                value = value.toLowerCase();
            }
            glob = value;
        }

        @Override
        protected boolean match(NBTTagString nbt) {
            String value = nbt.func_150285_a_();
            return matchPartial(value, 0, value.length(), 0, glob.length());
        }

        private boolean matchPartial(String value, int curV, int maxV, int curG, int maxG) {
            for (; curG < maxG; curG++, curV++) {
                char g = glob.charAt(curG);
                if (g == STAR) {
                    while (true) {
                        if (matchPartial(value, curV, maxV, curG + 1, maxG)) {
                            return true;
                        }
                        if (curV >= maxV) {
                            break;
                        }
                        curV++;
                    }
                    return false;
                } else if (curV >= maxV) {
                    break;
                } else if (g == SINGLE) {
                    continue;
                }
                if (g == ESCAPE && curG + 1 < maxG) {
                    curG++;
                    g = glob.charAt(curG);
                }
                if (!matchChar(g, value.charAt(curV))) {
                    return false;
                }
            }
            return curG == maxG && curV == maxV;
        }

        private boolean matchChar(char a, char b) {
            return a == (caseSensitive ? b : Character.toLowerCase(b));
        }
    }
}