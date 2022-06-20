package dev.cromo29.mines.objects;

import org.bukkit.Material;

public class MineBlock {

    private final Material material;
    private final byte data;
    private double percentage;

    public MineBlock(Material material, byte data, double percentage) {
        this.material = material;
        this.data = data;
        this.percentage = percentage;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {

        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        this.percentage = percentage;
    }

    public MineBlock clone() {
        return new MineBlock(material, data, percentage);
    }
}
