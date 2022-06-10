package dev.cromo29.mines.service;

import com.google.gson.Gson;
import dev.cromo29.durkcore.hologram.Hologram;
import dev.cromo29.durkcore.specificutils.LocationUtil;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.specificutils.PlayerUtil;
import dev.cromo29.durkcore.util.GsonManager;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.blocks.PlacableBlock;
import dev.cromo29.mines.blocks.WorkloadThread;
import dev.cromo29.mines.objects.Mine;
import dev.cromo29.mines.objects.MineBlock;
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
        for (Mine mine : getMines()) {
            if (Utils.containsLocationBetwen(location, mine.getStart(), mine.getEnd())) return mine;
        }

        return null;
    }

    @Override
    public boolean hasMine(String name) {
        return mineMap.containsKey(name.toLowerCase());
    }

    @Override
    public boolean hasMine(Location location) {
        return getMine(location) != null;
    }

    @Override
    public void saveMine(Mine mine, boolean async) {

        if (async) {

            if (!MinePlugin.get().isEnabled()) {
                saveMine(mine, false);
                return;
            }

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

            if (!MinePlugin.get().isEnabled()) {
                removeMine(mine, false);
                return;
            }

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
            MineBlock mineBlock = getSortedMineBlock(mine);

            if (block.getType() == mineBlock.getMaterial() && block.getData() == mineBlock.getData()) continue;

            workloadThread.addLoad(new PlacableBlock(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ(), mineBlock.getMaterial(), mineBlock.getData()));
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                if (workloadThread.hasEnded()) {
                    cancel();

                    mine.setCurrentBlocks(mine.getMaxBlocks());
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

                String name = map.get("name").toString();
                Location start = LocationUtil.unserializeLocation(map.get("start").toString());
                Location end = LocationUtil.unserializeLocation(map.get("end").toString());
                double resetPercentage = (double) map.get("resetPercentage");
                double maxBlocks = (double) map.get("maxBlocks");

                List<MineBlock> blocks = new LinkedList<>(Arrays.asList(new Gson().fromJson(map.get("blocks").toString(), MineBlock[].class)));

                Mine mine = new Mine(name, start, end, blocks, resetPercentage, 0, (long) maxBlocks);
                mine.setCurrentBlocks(Utils.getCurrentBlocksFrom(mine));

                if (map.get("hologram") != null) {
                    Hologram hologram = new Hologram(MinePlugin.get(), LocationUtil.unserializeLocation(map.get("hologram").toString()), mineName.toLowerCase());

                    hologram.addLine(" &fMina &d" + mine.getName() + "&f! ");
                    hologram.addLine(" &fPorcentagem para resetar &d" + NumberUtil.formatNumber(mine.getCurrentPercentage()) + "% &fde &d" + NumberUtil.formatNumber(mine.getResetPercentage()) + "%&f! ");

                    hologram.setRemoveOnDisable(true);
                    hologram.setup();

                    mine.setHologram(hologram);
                }

                setMine(mine);

            } catch (Exception exception) {
                MinePlugin.get().log(" <c>Erro ao carregar mina: <f>" + mineName);
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

    private MineBlock getSortedMineBlock(Mine mine) {
        double totalPercentage = 0;

        for (MineBlock mineBlock : mine.getMineBlocks()) {
            totalPercentage += mineBlock.getPercentage();
        }

        int sorted = -1;
        double random = Math.random() * totalPercentage;

        for (int index = 0; index < mine.getMineBlocks().size(); index++) {
            random -= mine.getMineBlocks().get(index).getPercentage();

            if (random <= 0) {
                sorted = index;
                break;
            }
        }

        return mine.getMineBlocks().get(sorted);
    }
}
