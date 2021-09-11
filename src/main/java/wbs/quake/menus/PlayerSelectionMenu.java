package wbs.quake.menus;

import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;

public abstract class PlayerSelectionMenu<T extends MenuSelectable> extends PlayerSpecificMenu {
    protected int minSlot, maxSlot;

    public PlayerSelectionMenu(WbsQuake plugin, QuakePlayer player, String title, int rows, String id) {
        super(plugin, player, title, rows, id);

        minSlot = 0;
        maxSlot = getMaxSlot();
    }

    public void setCurrent(T selected) {
        SelectableSlot<T> slot = getSlotFor(selected);

        setCurrent(slot);
    }

    protected abstract void setCurrent(SelectableSlot<T> slot);

    protected abstract SelectableSlot<T> getSlotFor(T selected);

    protected void addSlot(T selected) {
        SelectableSlot<T> slot = getSlotFor(selected);

        setNextFreeSlot(minSlot, maxSlot, slot);
    }

    protected void addSlot(SelectableSlot<T> slot) {
        setNextFreeSlot(minSlot, maxSlot, slot);
    }

    public void updateSelected(SelectableSlot<T> slot) {
        update();
    }
}
