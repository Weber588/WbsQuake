package wbs.quake.player;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticType;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.GunSkin;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.quake.cosmetics.trails.Trail;

import java.util.HashMap;
import java.util.Map;

public class PlayerCosmetics {

    public PlayerCosmetics() {
        CosmeticsStore store = CosmeticsStore.getInstance();

        trail = store.getDefaultTrail();
        setCosmetic(trail);

        skin = store.getSkin("default");
        setCosmetic(skin);
    }

    public PlayerCosmetics(@NotNull ConfigurationSection section) {
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

    public void writeToConfig(ConfigurationSection section, String path) {
        section.set(path + ".trail", trail.getId());
    }

    private final Map<CosmeticType, SelectableCosmetic<?>> cosmetics = new HashMap<>();

    public SelectableCosmetic<?> getCosmetic(CosmeticType type) {
        return cosmetics.get(type);
    }

    public <T extends SelectableCosmetic<T>> void setCosmetic(@NotNull SelectableCosmetic<T> cosmetic) {
        cosmetics.put(cosmetic.getCosmeticType(), cosmetic);

        switch (cosmetic.getCosmeticType()) {
            case TRAIL:
                trail = (Trail) cosmetic;
                break;
            case SKIN:
                break;
            case ARMOUR:
                break;
            case KILL_MESSAGE:
                break;
            case KILL_EFFECT:
                break;
            case KILL_SOUND:
                break;
            case SHOOT_SOUND:
                break;
        }
    }
}
