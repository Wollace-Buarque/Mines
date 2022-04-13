package dev.cromo29.mines.manager;

import dev.cromo29.durkcore.SpecificUtils.NumberUtil;
import dev.cromo29.durkcore.Util.MakeItem;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.durkcore.hologram.Hologram;
import dev.cromo29.durkcore.hologram.HologramLine;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.object.Mine;
import dev.cromo29.mines.service.MineServiceImpl;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineManager {

    private final MessageManager messageManager;

    private final MineServiceImpl mineService;

    private final Map<String, Mine> setupMap;

    public MineManager() {
        this.messageManager = new MessageManager();

        this.mineService = new MineServiceImpl();

        this.setupMap = new HashMap<>();
    }

    public void createMine(Player player, String name, double resetPercentage) {

        if (setupMap.containsKey(player.getName().toLowerCase())) {
            messageManager.sendMessage(player, "Creation cancelled");
            setupMap.remove(player.getName().toLowerCase());
            return;
        }

        if (mineService.hasMine(name)) {
            messageManager.sendMessage(player, "Already created",
                    "{name}", name);
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            messageManager.sendMessage(player, "Full inventory");
            return;
        }

        ItemStack build = new MakeItem(Material.STONE_PICKAXE)
                .setName(" <7>Criar mina")
                .build();

        if (!player.getInventory().contains(build)) player.getInventory().addItem(build);

        player.updateInventory();

        setupMap.put(player.getName().toLowerCase(), new Mine(name, resetPercentage));

        messageManager.sendMessage(player, "Started creation",
                "{name}", name);

        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
    }

    public void deleteMine(Player player, String name, boolean clear) {

        if (!mineService.hasMine(name)) {
            messageManager.sendMessage(player, "Not created",
                    "{name}", name);
            return;
        }

        Mine mine = mineService.getMine(name);

        mineService.deleteMine(mine);
        mineService.removeMine(mine, mineService.getMines().size() > 5);

        if (clear) mineService.clearMine(mine);

        messageManager.sendMessage(player, "Mine deleted",
                "{name}", mine.getName());
    }

    public void listMines(Player player) {

        List<Mine> mines = mineService.getMines();

        if (mines.isEmpty()) {
            messageManager.sendMessage(player, "No mine");
            return;
        }

        String minesText = TXT.parse(TXT.createString(mines.stream().map(Mine::getName).toArray(String[]::new), 0, "<e>, <f>"));

        messageManager.sendMessage(player, "Mines",
                "{mines}", minesText);
    }

    public void createHologram(Player player, String name) {

        if (!mineService.hasMine(name)) {
            messageManager.sendMessage(player, "Not created",
                    "{name}", name);
            return;
        }

        Mine mine = mineService.getMine(name);

        if (mine.getHologram() != null) mine.getHologram().clear();

        Hologram hologram = new Hologram(MinePlugin.get(), player.getLocation(), mine.getName().toLowerCase());

        hologram.addLine(" &fMina &d" + mine.getName() + "&f! ");
        hologram.addLine(" &fPorcentagem para resetar &d" + NumberUtil.formatNumber(mine.getCurrentPercentage()) + "% &fde &d" + NumberUtil.formatNumber(mine.getResetPercentage()) + "%&f! ");

        hologram.setRemoveOnDisable(true);

        hologram.setup();

        mine.setHologram(hologram);

        messageManager.sendMessage(player, "Hologram created",
                "{name}", mine.getName());
    }

    public void hologramTask() {

        MinePlugin.get().getServer().getScheduler().runTaskTimerAsynchronously(MinePlugin.get(), () -> {

            for (Mine mine : mineService.getMines()) {

                if (mine.getHologram() == null) continue;

                final Hologram hologram = mine.getHologram();
                final List<HologramLine> hologramLines = hologram.getHologramLines();

                String line = " &fPorcentagem para resetar &d" + NumberUtil.formatNumber(mine.getCurrentPercentage()) + "% &fde &d" + NumberUtil.formatNumber(mine.getResetPercentage()) + "%&f! ";

                for (HologramLine hologramLine : hologramLines) {

                    if (hologramLine.getText().contains("%")) {
                        hologramLine.setText(line);
                        break;
                    }
                }

            }

        }, 140, 100);

    }

    public MessageManager getMessageManager() {
        return messageManager;
    }


    public MineServiceImpl getMineService() {
        return mineService;
    }


    public Map<String, Mine> getSetupMap() {
        return setupMap;
    }
}
