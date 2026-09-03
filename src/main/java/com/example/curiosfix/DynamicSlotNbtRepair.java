package com.example.curiosfix;

import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class DynamicSlotNbtRepair {
    private DynamicSlotNbtRepair() {
    }

    public static void clearCachedSlotModifiers(
            Map<String, ICurioStacksHandler> handlers, LivingEntity wearer) {
        int cachedBefore = handlers.values().stream()
                .mapToInt(handler -> handler.getCachedModifiers().size()).sum();
        if (cachedBefore == 0) {
            return;
        }
        Multimap<String, AttributeModifier> active = HashMultimap.create();
        for (Map.Entry<String, ICurioStacksHandler> entry : handlers.entrySet()) {
            ICurioStacksHandler handler = entry.getValue();
            IDynamicStackHandler stacks = handler.getStacks();
            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                ItemStack stack = stacks.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                SlotContext context = new SlotContext(entry.getKey(), wearer, slot, false,
                        handler.getRenders().size() > slot && handler.getRenders().get(slot));
                Multimap<Attribute, AttributeModifier> modifiers;
                try {
                    modifiers = CuriosApi.getAttributeModifiers(
                            context, CuriosApi.getSlotUuid(context), stack);
                } catch (RuntimeException exception) {
                    CuriosFix.LOGGER.debug("Unable to inspect active slot modifier for {}[{}]",
                            entry.getKey(), slot, exception);
                    continue;
                }
                for (Map.Entry<Attribute, AttributeModifier> modifier : modifiers.entries()) {
                    if (modifier.getKey() instanceof SlotAttribute slotAttribute) {
                        active.put(slotAttribute.getIdentifier(), modifier.getValue());
                    }
                }
            }
        }

        Map<String, Set<AttributeModifier>> cached = new HashMap<>();
        handlers.forEach((identifier, handler) ->
                cached.put(identifier, handler.getCachedModifiers()));
        removeActiveCachedMarkers(cached, active);
        int stale = handlers.values().stream()
                .mapToInt(handler -> handler.getCachedModifiers().size()).sum();
        handlers.values().forEach(handler -> {
            if (!handler.getCachedModifiers().isEmpty()) {
                handler.clearCachedModifiers();
            }
        });
        if (cachedBefore > 0) {
            ICurioStacksHandler charm = handlers.get("charm");
            CuriosFix.LOGGER.debug(
                    "[CuriosFix] cache cleanup cached={} activeMatched={} stale={} charmSlots={}",
                    cachedBefore, cachedBefore - stale, stale,
                    charm == null ? -1 : charm.getSlots());
        }
    }

    static void removeActiveCachedMarkers(
            Map<String, Set<AttributeModifier>> cached,
            Multimap<String, AttributeModifier> active) {
        for (Map.Entry<String, Collection<AttributeModifier>> entry
                : active.asMap().entrySet()) {
            Set<AttributeModifier> target = cached.get(entry.getKey());
            if (target != null) {
                target.removeAll(entry.getValue());
            }
        }
    }

}
