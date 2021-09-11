package wbs.quake.player;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
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

        deathSound = store.getDeathSound("default");
        setCosmetic(deathSound);

        shootSound = store.getShootSound("default");
        setCosmetic(shootSound);
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

        String deathSoundString = section.getString("death-sound");
        deathSound = store.getDeathSound(deathSoundString);
        setCosmetic(deathSound);

        String shootSoundString = section.getString("shoot-sound");
        shootSound = store.getShootSound(shootSoundString);
        setCosmetic(shootSound);
    }

    @NotNull
    public Trail trail;
    @NotNull
    public GunSkin skin;
    @NotNull
    public DeathSound deathSound;
    @NotNull
    public ShootSound shootSound;

    public void writeToConfig(ConfigurationSection section, String path) {
        setIfNotDefault(section, path + ".trail", trail.getId());
        setIfNotDefault(section, path + ".shoot-sound", shootSound.getId());
        setIfNotDefault(section, path + ".death-sound", deathSound.getId());

        if (!CosmeticsStore.getInstance().getSkin("default").getId().equalsIgnoreCase(skin.getId())) {
            section.set(path + ".skin", skin.getId());
        } else {
            section.set(path + ".skin", null);
        }
    }

    private void setIfNotDefault(ConfigurationSection section, String path, String value) {
        section.set(path, value.equalsIgnoreCase("default") ? null : value);
    }

    private final Map<CosmeticType, SelectableCosmetic<?>> cosmetics = new HashMap<>();

    @NotNull
    public SelectableCosmetic<?> getCosmetic(CosmeticType type) {
        return cosmetics.get(type);
    }

    public <T extends SelectableCosmetic<T>> void setCosmetic(@NotNull SelectableCosmetic<T> cosmetic) {
        cosmetics.put(cosmetic.getCosmeticType(), cosmetic);

        cosmetic.onSelect(player, this);
    }
}
