package wbs.quake.upgrades;

import wbs.utils.util.WbsTime;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.time.Duration;

public enum UpgradePathType {
    TICKS, SECONDS, NUMBER, PERCENT;

    public String format(double value) {
        switch (this) {
            case TICKS:
                return WbsStringify.toString(Duration.ofMillis((long) (value / 20.0 * 1000)), false);
            case SECONDS:
                return WbsStringify.toString(Duration.ofMillis((long) (value * 1000)), false);
            case NUMBER:
                return value + "";
            case PERCENT:
                return "+" + value + "%";
        }

        return "?";
    }
}
