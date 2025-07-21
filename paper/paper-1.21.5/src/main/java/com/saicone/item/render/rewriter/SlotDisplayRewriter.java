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
            final var result = mapper.apply(player, new ItemStack(item.item()), view, slot);
            if (result.item() == null) {
                return null;
            } else if (result.edited()) {
                return new SlotDisplay.ItemSlotDisplay(result.item().getItemHolder());
            }
        } else if (display instanceof SlotDisplay.ItemStackSlotDisplay itemStack) {
            final var result = mapper.apply(player, itemStack.stack(), view, slot);
            if (result.item() == null) {
                return null;
            } else if (result.edited()) {
                return new SlotDisplay.ItemStackSlotDisplay(result.item());
            }
        } else if (display instanceof SlotDisplay.TagSlotDisplay tag) {
            // TODO: Check this implementation later to convert TagKey into ItemStack and viceversa
        } else if (display instanceof SlotDisplay.SmithingTrimDemoSlotDisplay smithing) {
            final SlotDisplay base = rewrite(mapper, player, view, smithing.base(), ItemSlot.Recipe.TRIM_BASE);
            if (base == null) {
                return null;
            }

            final SlotDisplay material = rewrite(mapper, player, view, smithing.material(), ItemSlot.Recipe.TRIM_ADDITION);
            if (material == null) {
                return null;
            }

            if (base == SlotDisplay.Empty.INSTANCE && material == SlotDisplay.Empty.INSTANCE) {
                return SlotDisplay.Empty.INSTANCE;
            }

            return new SlotDisplay.SmithingTrimDemoSlotDisplay(
                    base == SlotDisplay.Empty.INSTANCE ? smithing.base() : base,
                    material == SlotDisplay.Empty.INSTANCE ? smithing.material() : material,
                    smithing.pattern()
            );
        } else if (display instanceof SlotDisplay.WithRemainder withRemainder) {
            final SlotDisplay input = rewrite(mapper, player, view, withRemainder.input(), slot);
            if (input == null) {
                return null;
            }

            final SlotDisplay remainder = rewrite(mapper, player, view, withRemainder.remainder(), slot);
            if (remainder == null) {
                return null;
            }

            if (input == SlotDisplay.Empty.INSTANCE && remainder == SlotDisplay.Empty.INSTANCE) {
                return SlotDisplay.Empty.INSTANCE;
            }

            return new SlotDisplay.WithRemainder(
                    input == SlotDisplay.Empty.INSTANCE ? withRemainder.input() : input,
                    remainder == SlotDisplay.Empty.INSTANCE ? withRemainder.remainder() : remainder
            );
        } else if (display instanceof SlotDisplay.Composite composite) {
            boolean edited = false;
            final List<SlotDisplay> contents = new ArrayList<>();
            for (SlotDisplay content : composite.contents()) {
                final SlotDisplay result = rewrite(mapper, player, view, content, slot);
                if (result == null) {
                    return null;
                } else if (result == SlotDisplay.Empty.INSTANCE) {
                    contents.add(content);
                } else {
                    contents.add(result);
                    edited = true;
                }
            }

            if (edited) {
                return new SlotDisplay.Composite(contents);
            }
        }
        return SlotDisplay.Empty.INSTANCE;
    }
}
