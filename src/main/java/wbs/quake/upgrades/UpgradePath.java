package wbs.quake.upgrades;

import java.util.ArrayList;
import java.util.List;

public class UpgradePath {

    private final List<Double> values = new ArrayList<>();
    private final List<Double> prices = new ArrayList<>();

    private final String id;
    private final UpgradePathType pathType;

    public UpgradePath(String id, UpgradePathType pathType) {
        this.id = id;
        this.pathType = pathType;
    }

    public void addValuePricePair(double value, double price) {
        values.add(value);
        prices.add(price);
    }

    public double getValue(int index) {
        return values.get(index);
    }

    public double getPrice(int index) {
        return prices.get(index);
    }

    public int length() {
        return values.size();
    }

    public String getId() {
        return id;
    }

    public String format(double val) {
        return pathType.format(val);
    }
}
