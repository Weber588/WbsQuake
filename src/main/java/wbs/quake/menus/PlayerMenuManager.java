package wbs.quake.menus;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.WbsQuake;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PlayerMenuManager<T extends PlayerSpecificMenu> {

    private final Map<QuakePlayer, T> menus = new HashMap<>();
    public T getMenu(@NotNull QuakePlayer player, Class<T> clazz) {
        if (menus.containsKey(player)) return menus.get(player);

        T playerMenu;
        try {
            Constructor<? extends PlayerSpecificMenu> constructor = clazz.getConstructor(WbsQuake.class, QuakePlayer.class);

            playerMenu = clazz.cast(constructor.newInstance(WbsQuake.getInstance(), player));
        } catch (NoSuchMethodException |
                InvocationTargetException |
                InstantiationException |
                IllegalAccessException e) {
            e.printStackTrace();
            throw new ClassFormatError(clazz.getCanonicalName() + " failed to instantiate.");
        }

        menus.put(player, playerMenu);
        return playerMenu;
    }

}
