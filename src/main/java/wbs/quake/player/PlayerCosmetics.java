package wbs.quake.player;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.*;
import wbs.quake.cosmetics.trails.Trail;

import java.util.HashMap;
import java.util.Map;

public class PlayerCosmetics {

    private final QuakePlayer player;

    public PlayerCosmetics(QuakePlayer player) {
        this.player = player;
        CosmeticsStore store = CosmeticsStore.getInstance();

        trail = store.getDefaultTrail();
        setCosmetic(trail);

        skin = store.getSkin("default");
        setCosmetic(skin);
    }

    public PlayerCosmetics(QuakePlayer player, @NotNull ConfigurationSection section) {
        this.player = player;
        CosmeticsStore store = CosmeticsStore.getInstance();

        String trailId = section.getString("trail");
        trail = store.getTrail(trailId);
        setCosmetic(trail);

        String skinString = section.getString("skin");
        skin = store.getSkin(skinString);
        setCosmetic(skin);
    }

    public Trail trail;
    public GunSkin skin;
    public SelectableSound shootSound;
    public SelectableSound killSound;

    public void writeToConfig(ConfigurationSection section, String path) {
        section.set(path + ".trail", trail.getId());
        section.set(path + ".skin", skin.getId());
    }

    private final Map<CosmeticType, SelectableCosmetic<?>> cosmetics = new HashMap<>();

    public SelectableCosmetic<?> getCosmetic(CosmeticType type) {
        return cosmetics.get(type);
    }

    public <T extends SelectableCosmetic<T>> void setCosmetic(@NotNull SelectableCosmetic<T> cosmetic) {
        cosmetics.put(cosmetic.getCosmeticType(), cosmetic);

        cosmetic.onSelect(player, this);
    }
}
