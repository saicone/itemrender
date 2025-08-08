package com.saicone.item.render.rewriter;

import com.saicone.item.ItemSlot;
import com.saicone.item.ItemView;
import com.saicone.item.network.PacketItemMapper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface SlotDisplayRewriter<PlayerT> {

    @Nullable
    default SlotDisplay rewrite(@NotNull PacketItemMapper<PlayerT, ItemStack> mapper, @NotNull PlayerT player, @NotNull ItemView view, @NotNull SlotDisplay display, @NotNull ItemSlot slot) {
        if (display instanceof SlotDisplay.ItemSlotDisplay item) {
            final var result = mapper.context(player, new ItemStack(item.item()), view)
                    .withSlot(slot)
                    .apply();
            if (result.empty()) {
                return SlotDisplay.Empty.INSTANCE;
            } else if (result.edited()) {
                return new SlotDisplay.ItemSlotDisplay(result.item().getItemHolder());
            }
        } else if (display instanceof SlotDisplay.ItemStackSlotDisplay itemStack) {
            final var result = mapper.context(player, itemStack.stack(), view)
                    .withSlot(slot)
                    .apply();
            if (result.empty()) {
                return SlotDisplay.Empty.INSTANCE;
            } else if (result.edited()) {
                return new SlotDisplay.ItemStackSlotDisplay(result.item());
            }
        } else if (display instanceof SlotDisplay.TagSlotDisplay tag) {
            // TODO: Check this implementation later to convert TagKey into ItemStack and viceversa
        } else if (display instanceof SlotDisplay.SmithingTrimDemoSlotDisplay smithing) {
            final SlotDisplay base = rewrite(mapper, player, view, smithing.base(), ItemSlot.Recipe.TRIM_BASE);
            final SlotDisplay material = rewrite(mapper, player, view, smithing.material(), ItemSlot.Recipe.TRIM_ADDITION);

            if (base == null && material == null) {
                return null;
            }

            return new SlotDisplay.SmithingTrimDemoSlotDisplay(
                    base == null ? smithing.base() : base,
                    material == null ? smithing.material() : material,
                    smithing.pattern()
            );
        } else if (display instanceof SlotDisplay.WithRemainder withRemainder) {
            final SlotDisplay input = rewrite(mapper, player, view, withRemainder.input(), slot);
            final SlotDisplay remainder = rewrite(mapper, player, view, withRemainder.remainder(), slot);

            if (input == null && remainder == null) {
                return null;
            }

            return new SlotDisplay.WithRemainder(
                    input == null ? withRemainder.input() : input,
                    remainder == null ? withRemainder.remainder() : remainder
            );
        } else if (display instanceof SlotDisplay.Composite composite) {
            boolean edited = false;
            final List<SlotDisplay> contents = new ArrayList<>();
            for (SlotDisplay content : composite.contents()) {
                final SlotDisplay result = rewrite(mapper, player, view, content, slot);
                if (result == null) {
                    contents.add(content);
                } else {
                    contents.add(result);
                    edited = true;
                }
            }

            if (edited) {
                return new SlotDisplay.Composite(contents);
            }
            contents.clear();
        }
        return null;
    }
}
