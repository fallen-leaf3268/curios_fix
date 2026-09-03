package com.example.curiosfix;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicSlotNbtRepairTest {
    private static final UUID SLOT_UUID = UUID.fromString("f102de05-4f79-4a68-a31d-2408485b1f9f");

    @Test
    void removesActiveModifierProvidedFromAnotherSlotType() {
        AttributeModifier beltSlots = new AttributeModifier(SLOT_UUID, "belt_slots", 8,
                AttributeModifier.Operation.ADDITION);
        AttributeModifier charmSlots = new AttributeModifier(UUID.randomUUID(), "charm_slots", 1,
                AttributeModifier.Operation.ADDITION);
        Map<String, Set<AttributeModifier>> cached = new HashMap<>();
        cached.put("charm", new HashSet<>(Set.of(beltSlots, charmSlots)));
        Multimap<String, AttributeModifier> active = HashMultimap.create();
        active.put("charm", beltSlots);

        DynamicSlotNbtRepair.removeActiveCachedMarkers(cached, active);

        assertEquals(Set.of(charmSlots), cached.get("charm"));
    }

    @Test
    void retainsStaleModifierForSubsequentClear() {
        AttributeModifier activeModifier = new AttributeModifier(SLOT_UUID, "active", 1,
                AttributeModifier.Operation.ADDITION);
        AttributeModifier staleModifier = new AttributeModifier(UUID.randomUUID(), "stale", 2,
                AttributeModifier.Operation.ADDITION);
        Map<String, Set<AttributeModifier>> cached = new HashMap<>();
        cached.put("charm", new HashSet<>(Set.of(activeModifier, staleModifier)));
        Multimap<String, AttributeModifier> active = HashMultimap.create();
        active.put("charm", activeModifier);

        DynamicSlotNbtRepair.removeActiveCachedMarkers(cached, active);

        assertEquals(Set.of(staleModifier), cached.get("charm"));
    }

    @Test
    void doesNotAffectCachedModifiersForDifferentTargets() {
        AttributeModifier charmModifier = new AttributeModifier(SLOT_UUID, "charm", 1,
                AttributeModifier.Operation.ADDITION);
        AttributeModifier beltModifier = new AttributeModifier(UUID.randomUUID(), "belt", 2,
                AttributeModifier.Operation.ADDITION);
        Map<String, Set<AttributeModifier>> cached = new HashMap<>();
        cached.put("charm", new HashSet<>(Set.of(charmModifier)));
        cached.put("belt", new HashSet<>(Set.of(beltModifier)));
        Multimap<String, AttributeModifier> active = HashMultimap.create();
        active.put("charm", charmModifier);

        DynamicSlotNbtRepair.removeActiveCachedMarkers(cached, active);

        assertTrue(cached.get("charm").isEmpty());
        assertEquals(Set.of(beltModifier), cached.get("belt"));
    }
}
