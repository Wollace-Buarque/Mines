package dev.cromo29.mines.object;

import dev.cromo29.durkcore.SpecificUtils.LocationUtil;
import dev.cromo29.durkcore.hologram.Hologram;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mine {

    private String name;
    private Location start, end;
    private List<MineBlock> mineBlockList;
    private double resetPercentage;
    private Hologram hologram;
    private long currentBlocks, maxBlocks;
    private boolean reseting;

    public Mine(String name, double resetPercentage) {
        this(name, null, null, new ArrayList<>(), resetPercentage, 0, 0);
    }

    public Mine(String name, Location start, Location end, double resetPercentage) {
        this(name, start, end, new ArrayList<>(), resetPercentage, 0, 0);
    }

    public Mine(String name, Location start, Location end, List<MineBlock> mineBlockList, double resetPercentage, long currentBlocks, long maxBlocks) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.mineBlockList = mineBlockList;
        this.resetPercentage = resetPercentage;
        this.currentBlocks = currentBlocks;
        this.maxBlocks = maxBlocks;
        this.reseting = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getStart() {
        return start;
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public Location getEnd() {
        return end;
    }

    public void setEnd(Location end) {
        this.end = end;
    }

    public List<MineBlock> getMineBlockList() {
        return mineBlockList;
    }

    public void setMineBlockList(List<MineBlock> mineBlockList) {
        this.mineBlockList = mineBlockList;
    }

    public double getResetPercentage() {
        return resetPercentage;
    }

    public void setResetPercentage(double resetPercentage) {
        this.resetPercentage = resetPercentage;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public long getCurrentBlocks() {
        return currentBlocks;
    }

    public void setCurrentBlocks(long currentBlocks) {
        this.currentBlocks = currentBlocks;
    }

    public long getMaxBlocks() {
        return maxBlocks;
    }

    public void setMaxBlocks(long maxBlocks) {
        this.maxBlocks = maxBlocks;
    }

    public boolean isReseting() {
        return reseting;
    }

    public void setReseting(boolean reseting) {
        this.reseting = reseting;
    }

    public double getCurrentPercentage() {
        double maxBlocks = this.maxBlocks == 0 ?  1 : this.maxBlocks;

        return (currentBlocks / maxBlocks) * 100;
    }

    public Map<String, Object> getData() {
        final Map<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("start", LocationUtil.serializeSimpleLocation(start));
        map.put("end", LocationUtil.serializeSimpleLocation(end));
        map.put("resetPercentage", resetPercentage);
        map.put("blocks", mineBlockList);
        map.put("currentBlocks", currentBlocks);
        map.put("maxBlocks", maxBlocks);

        return map;
    }
}
