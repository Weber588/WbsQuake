package wbs.quake.player;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeDB;
import wbs.quake.cosmetics.*;
import wbs.quake.cosmetics.trails.Trail;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.database.WbsRecord;

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

        killMessage = store.getKillMessage("default");
        setCosmetic(killMessage);
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

        String killMessageString = section.getString("kill-message");
        killMessage = store.getKillMessage(killMessageString);
        setCosmetic(killMessage);
    }

    public PlayerCosmetics(QuakePlayer player, WbsRecord record) {
        this.player = player;
        CosmeticsStore store = CosmeticsStore.getInstance();

        String trailId = record.getOrDefault(QuakeDB.trailField, String.class);
        trail = store.getTrail(trailId);
        setCosmetic(trail);

        String skinString = record.getOrDefault(QuakeDB.skinField, String.class);
        skin = store.getSkin(skinString);
        setCosmetic(skin);

        String deathSoundString = record.getOrDefault(QuakeDB.deathSoundField, String.class);
        deathSound = store.getDeathSound(deathSoundString);
        setCosmetic(deathSound);

        String shootSoundString = record.getOrDefault(QuakeDB.shootSoundField, String.class);
        shootSound = store.getShootSound(shootSoundString);
        setCosmetic(shootSound);

        String killMessageString = record.getOrDefault(QuakeDB.killMessageField, String.class);
        killMessage = store.getKillMessage(killMessageString);
        setCosmetic(killMessage);
    }

    public void toRecord(WbsRecord record) {
        record.setField(QuakeDB.trailField, trail.getId());
        record.setField(QuakeDB.skinField, skin.getId());
        record.setField(QuakeDB.deathSoundField, deathSound.getId());
        record.setField(QuakeDB.shootSoundField, shootSound.getId());
        record.setField(QuakeDB.killMessageField, killMessage.getId());
    }

    @NotNull
    public Trail trail;
    @NotNull
    public GunSkin skin;
    @NotNull
    public DeathSound deathSound;
    @NotNull
    public ShootSound shootSound;
    @NotNull
    public KillMessage killMessage;

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

    private final Map<CosmeticType, SelectableCosmetic> cosmetics = new HashMap<>();

    @NotNull
    public SelectableCosmetic getCosmetic(CosmeticType type) {
        return cosmetics.get(type);
    }

    public void setCosmetic(@NotNull SelectableCosmetic cosmetic) {
        cosmetics.put(cosmetic.getCosmeticType(), cosmetic);

        cosmetic.onSelect(player, this);
    }
}
