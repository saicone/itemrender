package com.saicone.item;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum ItemView {

    /**
     * Is the view when an item is set into inventory that player is seeing.
     */
    WINDOW,
    /**
     * Is the view when an item is set into a creative inventory.
     */
    WINDOW_CREATIVE,
    /**
     * Is the view of an item generated from creative player inventory.
     */
    WINDOW_SERVER,
    /**
     * Is the information about a recipe.
     */
    RECIPE,
    /**
     * Is the view when an item is listed into merchant inventory.
     */
    MERCHANT,
    /**
     * Is the view when an item is on entity equipment.
     */
    EQUIPMENT,
    /**
     * Is the view when an item is rendered as entity (eye of ender, fireball, rocket... etc.), on block entity (like item frame) or armor stand.
     */
    METADATA,
    /**
     * Is the view when an item is on text component sent through chat.
     */
    CHAT;

    public static final ItemView[] CLIENTBOUND = new ItemView[] { WINDOW, MERCHANT, EQUIPMENT, METADATA };
    public static final ItemView[] SERVERBOUND = new ItemView[] { WINDOW_SERVER };
    public static final ItemView[] FIRST_PERSON = new ItemView[] { WINDOW, MERCHANT };
    public static final ItemView[] THIRD_PERSON = new ItemView[] { EQUIPMENT, METADATA };

    public boolean isFirstPerson() {
        switch (this) {
            case WINDOW:
            case RECIPE:
            case MERCHANT:
                return true;
            default:
                return false;
        }
    }

    public boolean isThirdPerson() {
        return this == EQUIPMENT || this == METADATA;
    }

    @NotNull
    public static Optional<ItemView> of(@NotNull String name) {
        for (ItemView value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
