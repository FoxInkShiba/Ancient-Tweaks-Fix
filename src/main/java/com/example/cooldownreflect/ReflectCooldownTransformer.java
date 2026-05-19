package com.example.cooldownreflect;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class ReflectCooldownTransformer implements IClassTransformer {
    private static final String[] TARGETS = {
            "com.keletu.ancienttweaks.baubles.ItemCrabClaw",
            "com.keletu.ancienttweaks.baubles.ItemBaroClaw",
            "com.keletu.ancienttweaks.baubles.ItemTheAbsorber"
    };

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;
        for (String t : TARGETS) {
            if (transformedName.equals(t)) {
                return transformClass(bytes, transformedName);
            }
        }
        return bytes;
    }

    private byte[] transformClass(byte[] bytes, String className) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if ("onEntityHurt".equals(name) && "(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V".equals(desc)) {
                    // 使用 AdviceAdapter 可以自动处理栈帧，但为了不增加依赖，手动处理
                    return new MethodVisitor(Opcodes.ASM5, mv) {
                        @Override
                        public void visitCode() {
                            // 插入调用 ReflectCooldownHelper.shouldSkip(event)
                            // 如果返回 true，则直接 return
                            mv.visitVarInsn(Opcodes.ALOAD, 1);  // 加载 event 参数
                            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/example/cooldownreflect/ReflectCooldownHelper",
                                    "shouldSkip",
                                    "(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)Z",
                                    false);
                            Label labelContinue = new Label();
                            mv.visitJumpInsn(Opcodes.IFEQ, labelContinue);
                            mv.visitInsn(Opcodes.RETURN);
                            mv.visitLabel(labelContinue);
                            // 手动添加栈帧，因为这是一个分支目标
                            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                            super.visitCode();
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}