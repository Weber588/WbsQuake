package wbs.quake.menus;

import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;

public abstract class PlayerSelectionMenu<T extends MenuSelectable> extends PlayerSpecificMenu {
    public PlayerSelectionMenu(WbsQuake plugin, QuakePlayer player, String title, int rows, String id) {
        super(plugin, player, title, rows, id);
    }

    public void setCurrent(T selected) {
        SelectableSlot<T> slot = getSlotFor(selected);

        setCurrent(slot);
    }

    protected abstract void setCurrent(SelectableSlot<T> slot);

    protected abstract SelectableSlot<T> getSlotFor(T selected);

    protected void addSlot(T selected) {
        SelectableSlot<T> slot = getSlotFor(selected);

        setNextFreeSlot(slot);
    }

    protected void addSlot(SelectableSlot<T> selectableSlot) {
        setNextFreeSlot(selectableSlot);
    }

    public void updateSelected(SelectableSlot<T> slot) {
        update();
    }
}
