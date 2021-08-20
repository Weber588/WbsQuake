package wbs.quake.upgrades;

import org.jetbrains.annotations.NotNull;

public class UpgradeableOption {

    @NotNull
    private final UpgradePath path;
    private int currentProgress;

    public UpgradeableOption(@NotNull UpgradePath path, int currentProgress) {
        this.path = path;
        this.currentProgress = currentProgress;
    }

    public int setCurrentProgress(int currentProgress) {
        int newProgress = currentProgress;

        if (newProgress >= path.length()) newProgress = path.length() - 1;
        if (newProgress < 0) newProgress = 0;

        this.currentProgress = newProgress;
        return newProgress;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public double val() {
        return path.getValue(currentProgress);
    }

    public int intVal() {
        return (int) val();
    }

    public String getId() {
        return path.getId();
    }

    @NotNull
    public UpgradePath getPath() {
        return path;
    }

    public String formattedValue() {
        return path.format(val());
    }
}
