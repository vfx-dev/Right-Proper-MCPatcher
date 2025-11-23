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

package com.falsepattern.mcpatcher.internal.asm;

import lombok.Data;
import lombok.val;
import lombok.var;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class CITIconReplacementInjector implements IClassTransformer {
    // @formatter:off
    private static final String NAME_GET_ICON_INDEX_DEOBF   = "getIconIndex";
    private static final String NAME_GET_ICON_INDEX_OBF     = "func_77650_f";
    private static final String NAME_GET_ICON               = "getIcon";

    private static final String DESC_GET_ICON_INDEX         = "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/IIcon;";
    private static final String DESC_GET_ICON_0             = "(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/util/IIcon;";
    private static final String DESC_GET_ICON_1             = "(Lnet/minecraft/item/ItemStack;ILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/util/IIcon;";

    private static final String NAME_GET_ICON_INDEX_RENAMED = "mcp$renamed$getIconIndex";
    private static final String NAME_GET_ICON_RENAMED       = "mcp$renamed$getIcon";

    private static final MethodDecl DECL_GET_ICON_INDEX_DEOBF   = new MethodDecl(NAME_GET_ICON_INDEX_DEOBF, DESC_GET_ICON_INDEX);
    private static final MethodDecl DECL_GET_ICON_INDEX_OBF     = new MethodDecl(NAME_GET_ICON_INDEX_OBF,   DESC_GET_ICON_INDEX);
    private static final MethodDecl DECL_GET_ICON_0             = new MethodDecl(NAME_GET_ICON,             DESC_GET_ICON_0);
    private static final MethodDecl DECL_GET_ICON_1             = new MethodDecl(NAME_GET_ICON,             DESC_GET_ICON_1);

    private static final MethodDecl[] POTENTIAL_CANDIDATES = {
            DECL_GET_ICON_INDEX_DEOBF,
            DECL_GET_ICON_INDEX_OBF,
            DECL_GET_ICON_0,
            DECL_GET_ICON_1
    };
    private static final int POTENTIAL_CANDIDATE_COUNT = POTENTIAL_CANDIDATES.length;

    private static final Map<MethodDecl, String> MAPPINGS;
    static {
        MAPPINGS = new HashMap<>();
        MAPPINGS.put(DECL_GET_ICON_INDEX_DEOBF, NAME_GET_ICON_INDEX_RENAMED);
        MAPPINGS.put(DECL_GET_ICON_INDEX_OBF,   NAME_GET_ICON_INDEX_RENAMED);
        MAPPINGS.put(DECL_GET_ICON_0,           NAME_GET_ICON_RENAMED);
        MAPPINGS.put(DECL_GET_ICON_1,           NAME_GET_ICON_RENAMED);
    }

    private static final String INTERNAL_ITEM = "net/minecraft/item/Item";
    private static final String ITEM = "net.minecraft.item.Item";
    private static final Map<String, Boolean> ITEM_SUBCLASS_MEMOIZATION = new HashMap<>(1024, 0.2F);
    static {
        ITEM_SUBCLASS_MEMOIZATION.put(INTERNAL_ITEM, true);
    }
    // @formatter:on

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || transformedName.equals(ITEM)) {
            return basicClass;
        }
        if (!isItemSubclass(transformedName.replace('.', '/'))) {
            return basicClass;
        }
        val cn = new ClassNode();

        new ClassReader(basicClass).accept(cn, 0);
        if (!isValidTarget(cn)) {
            return basicClass;
        }

        boolean modified = false;
        val methodCount = cn.methods.size();
        for (var i = 0; i < methodCount; i++) {
            var method = cn.methods.get(i);
            for (val mapping : MAPPINGS.entrySet()) {
                if (tryTransform(cn, method, mapping.getKey(), mapping.getValue())) {
                    modified = true;
                    break;
                }
            }
        }
        if (modified) {
            final ClassWriter writer = new ClassWriter(0);
            cn.accept(writer);
            return writer.toByteArray();
        } else {
            return basicClass;
        }
    }

    private static boolean tryTransform(ClassNode cn, MethodNode method, MethodDecl decl, String newName) {
        if (!decl.matches(method)) {
            return false;
        }
        method.name = newName;

        val insts = method.instructions.iterator();
        while (insts.hasNext()) {
            val inst = insts.next();
            if (inst instanceof MethodInsnNode) {
                val insnNode = (MethodInsnNode) inst;
                if (!insnNode.owner.equals(cn.name) &&
                    !insnNode.owner.equals(cn.superName) &&
                    !isItemSubclass(insnNode.owner)) {
                    continue;
                }
                for (val mapping : MAPPINGS.entrySet()) {
                    if (mapping.getKey()
                               .matches(insnNode)) {
                        insnNode.name = mapping.getValue();
                        break;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isItemSubclass(String className) {
        if (className == null) {
            return false;
        }

        val v = ITEM_SUBCLASS_MEMOIZATION.get(className);
        if (v != null) {
            return v;
        }

        if (className.equals(INTERNAL_ITEM)) {
            ITEM_SUBCLASS_MEMOIZATION.put(className, true);
            return true;
        }

        val classBytes = bytesFromInternalName(className);
        if (classBytes == null) {
            ITEM_SUBCLASS_MEMOIZATION.put(className, false);
            return false;
        }

        val isc = isItemSubclass(new ClassReader(classBytes).getSuperName());
        ITEM_SUBCLASS_MEMOIZATION.put(className, isc);
        return isc;
    }

    /**
     * Detect classes that can be patched based on their methods. This avoids unnecessary parsing of superclasses.
     */
    private static boolean isValidTarget(ClassNode cn) {
        val methodCount = cn.methods.size();
        for (var i = 0; i < methodCount; i++) {
            var method = cn.methods.get(i);
            for (int j = 0; j < POTENTIAL_CANDIDATE_COUNT; j++) {
                if (POTENTIAL_CANDIDATES[j].matches(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static byte[] bytesFromInternalName(String internalName) {
        val classLoader = Launch.classLoader;
        val in = classLoader.getResourceAsStream(internalName + ".class");
        if (in == null) {
            return null;
        }
        try {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    public static final class MethodDecl {
        private final String name;
        private final String desc;

        public boolean matches(MethodNode node) {
            return node.name.equals(name) && node.desc.equals(desc);
        }

        public boolean matches(MethodInsnNode insnNode) {
            return insnNode.name.equals(name) && insnNode.desc.equals(desc);
        }
    }
}
