package com.example.cooldownreflect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReflectCooldownHelper {
    private static final Map<UUID, Long> LAST_REFLECT_TICK = new HashMap<>();

    public static boolean shouldSkip(LivingHurtEvent event) {
        if (event == null) return false;
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) return false;
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        UUID id = attacker.getUniqueID();
        long now = attacker.getEntityWorld().getTotalWorldTime();
        Long last = LAST_REFLECT_TICK.get(id);
        if (last != null && now - last < 1) {
            return true;
        }
        LAST_REFLECT_TICK.put(id, now);
        return false;
    }
}