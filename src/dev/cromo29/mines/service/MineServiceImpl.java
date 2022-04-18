package dev.cromo29.mines.service;

import dev.cromo29.durkcore.SpecificUtils.LocationUtil;
import dev.cromo29.durkcore.SpecificUtils.PlayerUtil;
import dev.cromo29.durkcore.Util.GsonManager;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.blocks.PlacableBlock;
import dev.cromo29.mines.blocks.WorkloadThread;
import dev.cromo29.mines.object.Mine;
import dev.cromo29.mines.object.MineBlock;
import dev.cromo29.mines.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class MineServiceImpl implements IMineService {

    private final Map<String, Mine> mineMap;
    private final GsonManager storageFile;

    public MineServiceImpl() {
        this.mineMap = new HashMap<>();

        final String path = MinePlugin.get().getDataFolder().getPath() + File.separator + "storage";

        this.storageFile = new GsonManager(path, "storage.json").prepareGson();
    }

    @Override
    public void setMine(Mine mine) {
        mineMap.put(mine.getName().toLowerCase(), mine);
    }

    @Override
    public void deleteMine(Mine mine) {
        mineMap.remove(mine.getName().toLowerCase());
    }

    @Override
    public List<Mine> getMines() {
        return new ArrayList<>(mineMap.values());
    }

    @Override
    public Mine getMine(String name) {
        return mineMap.get(name.toLowerCase());
    }

    @Override
    public Mine getMine(Location location) {
        return mineMap.values().stream()
                .filter(mine -> Utils.containsLocationBetwen(location, mine.getStart(), mine.getEnd()))
                .findFirst().orElse(null);
    }

    @Override
    public boolean hasMine(String name) {
        return mineMap.containsKey(name.toLowerCase());
    }

    @Override
    public boolean hasMine(Location location) {
        return mineMap.values().stream()
                .anyMatch(mine -> Utils.containsLocationBetwen(location, mine.getStart(), mine.getEnd()));
    }

    @Override
    public void saveMine(Mine mine, boolean async) {

        if (async) {
            TXT.runAsynchronously(MinePlugin.get(), () -> {
                storageFile.put(mine.getName().toLowerCase(), mine.getData());
                storageFile.save();
            });
        } else {
            storageFile.put(mine.getName().toLowerCase(), mine.getData());
            storageFile.save();
        }

    }

    @Override
    public void removeMine(Mine mine, boolean async) {

        if (async) {
            TXT.runAsynchronously(MinePlugin.get(), () -> {
                storageFile.remove(mine.getName().toLowerCase());
                storageFile.save();
            });
        } else {
            storageFile.remove(mine.getName().toLowerCase());
            storageFile.save();
        }

    }

    @Override
    public void resetMine(Mine mine) {

        if (mine.isReseting()) return;

        mine.setReseting(true);

        final MinePlugin plugin = MinePlugin.get();

        List<Block> blocks = Utils.getBlocksBetweenLocations(mine.getStart(), mine.getEnd());

        WorkloadThread workloadThread = new WorkloadThread();

        for (Block block : blocks) {
            int random = new Random().nextInt(100) + 1;

            if (random == 100) random -= 1;

            for (MineBlock mineBlock : mine.getMineBlocks()) {

                if (random >= mineBlock.getMinPercentage() && random < mineBlock.getMaxPercentage()) {

                    if (block.getType() == mineBlock.getMaterial() && block.getData() == mineBlock.getData()) break;

                    workloadThread.addLoad(new PlacableBlock(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ(), mineBlock.getMaterial(), mineBlock.getData()));
                    break;
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                if (workloadThread.hasEnded()) {
                    cancel();

                    mine.setCurrentBlocks(mine.getCurrentBlocks());
                    mine.setReseting(false);

                    for (Player player : Utils.getPlayersBetweenLocations(mine.getStart(), mine.getEnd())) {

                        for (int y = 256; y > 0; y--) {
                            Block block = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());

                            if (block == null || block.getType() == Material.AIR) continue;

                            Location toTeleport = block.getLocation();
                            toTeleport.setY(toTeleport.getBlockY() + 1.5);

                            player.teleport(toTeleport);
                            break;
                        }

                        PlayerUtil.sendActionBar(player, "Mina <d>" + mine.getName() + " <f>resetada!");
                    }

                    return;
                }

                for (Player player : Utils.getPlayersBetweenLocations(mine.getStart(), mine.getEnd())) {
                    PlayerUtil.sendActionBar(player, "Mina <d>" + mine.getName() + " <f>sendo resetada!");
                }

                workloadThread.run();

            }
        }.runTaskTimer(plugin, 0, 1);

    }

    @Override
    public void loadAll() {
        mineMap.clear();

        for (String mineName : storageFile.getDataPaths()) {

            try {

                Map<String, Object> map = storageFile.get(mineName).asMap();

                String name = (String) map.get("name");
                Location start = LocationUtil.unserializeLocation((String) map.get("start"));
                Location end = LocationUtil.unserializeLocation((String) map.get("end"));
                double resetPercentage = (double) map.get("resetPercentage");
                double currentBlocks = (double) map.get("currentBlocks");
                double maxBlocks = (double) map.get("maxBlocks");

                List<MineBlock> blocks = new ArrayList<>();

                for (Object object : (List<Object>) map.get("blocks")) {
                    String[] splitedString = object.toString().split(",");

                    Material material = Material.getMaterial(splitedString[0].split("=")[1]);
                    byte data = (byte) Double.parseDouble(splitedString[1].split("=")[1]);
                    double percentage = Double.parseDouble(splitedString[2].split("=")[1]);
                    double minPercentage = Double.parseDouble(splitedString[3].split("=")[1]);
                    double maxPercentage = Double.parseDouble(splitedString[4].split("=")[1].replace("}", ""));

                    MineBlock mineBlock = new MineBlock(material, data, percentage);
                    mineBlock.setMinPercentage(minPercentage);
                    mineBlock.setMaxPercentage(maxPercentage);

                    blocks.add(mineBlock);
                }

                Mine mine = new Mine(name, start, end, blocks, resetPercentage, (long) currentBlocks, (long) maxBlocks);
                setMine(mine);

            } catch (Exception exception) {
                MinePlugin.get().log(" <c>Erro ao carregar mina: " + mineName);
            }
        }

    }

    @Override
    public void clearMine(Mine mine) {
        final MinePlugin plugin = MinePlugin.get();

        if (mine.isReseting()) return;

        mine.setReseting(true);

        List<Block> blocks = Utils.getBlocksBetweenLocations(mine.getStart(), mine.getEnd());

        WorkloadThread workloadThread = new WorkloadThread();

        for (Block block : blocks) {
            if (block == null || block.getType() == Material.AIR) continue;

            workloadThread.addLoad(new PlacableBlock(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ(), Material.AIR, (byte) 0));
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                if (workloadThread.hasEnded()) {
                    cancel();

                    mine.setCurrentBlocks(0);
                    mine.setReseting(false);
                    return;
                }

                workloadThread.run();

            }
        }.runTaskTimer(plugin, 0, 1);

    }
}
