package dev.cromo29.mines.manager;

import dev.cromo29.durkcore.hologram.Hologram;
import dev.cromo29.durkcore.hologram.HologramLine;
import dev.cromo29.durkcore.inventory.Inv;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.object.Mine;
import dev.cromo29.mines.object.MineBlock;
import dev.cromo29.mines.service.IMineService;
import dev.cromo29.mines.service.MineServiceImpl;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MineManager {

    private final MessageManager messageManager;
    private final IMineService mineService;

    private final Map<String, Mine> setupMap;
    private final MinePlugin plugin;

    public MineManager() {
        this.messageManager = new MessageManager();
        this.mineService = new MineServiceImpl();

        this.setupMap = new HashMap<>();
        this.plugin = MinePlugin.get();
    }

    public void createMine(Player player, String name, double resetPercentage) {

        if (setupMap.containsKey(player.getName())) {
            messageManager.sendMessage(player, "Creation cancelled");
            setupMap.remove(player.getName());
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

        setupMap.put(player.getName(), new Mine(name, resetPercentage));

        messageManager.sendMessage(player, "Started creation",
                "{name}", name);

        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
    }

    public void deleteMine(CommandSender sender, String name, boolean clear) {

        if (!mineService.hasMine(name)) {
            messageManager.sendMessage(sender, "Not created",
                    "{name}", name);
            return;
        }

        Mine mine = mineService.getMine(name);

        mineService.deleteMine(mine);
        mineService.removeMine(mine, mineService.getMines().size() > 10);

        if (clear) mineService.clearMine(mine);

        messageManager.sendMessage(sender, "Mine deleted",
                "{name}", mine.getName());
    }

    public void listMines(CommandSender sender) {

        List<Mine> mines = mineService.getMines();

        if (mines.isEmpty()) {
            messageManager.sendMessage(sender, "No mine");
            return;
        }

        String minesText = TXT.parse(TXT.createString(mines.stream().map(Mine::getName).toArray(String[]::new), 0, "<e>, <f>"));

        messageManager.sendMessage(sender, "Mines", "{mines}", minesText);
    }

    public void resetMine(CommandSender sender, String mineName) {

        if (!mineService.hasMine(mineName)) {
            messageManager.sendMessage(sender, "Not created",
                    "{name}", mineName);
            return;
        }

        Mine mine = mineService.getMine(mineName);

        if (mine.isReseting()) {
            messageManager.sendMessage(sender, "Reset in progress",
                    "{name}", mineName);
            return;
        }

        mineService.resetMine(mine);

        messageManager.sendMessage(sender, "Mine reseted",
                "{name}", mineName);
    }

    public void editMine(Player player, String mineName) {

        if (!mineService.hasMine(mineName)) {
            messageManager.sendMessage(player, "Not created",
                    "{name}", mineName);
            return;
        }

        Mine mine = mineService.getMine(mineName);

        plugin.getMessageManager().sendMessage(player, "Blocks inventory");

        Inv inv = new Inv(54, "Blocos da mina:");
        inv.setIgnorePlayerInventoryClick(false, true);

        final List<MineBlock> toCheckAtEnd = mine.getMineBlocks();

        mine.getMineBlocks()
                .stream()
                .sorted(Comparator.comparing(MineBlock::getPercentage).reversed())
                .forEach(mineBlock -> {

                    double amountOfBlocksWirthPercentage = mineBlock.getPercentage() * mine.getMaxBlocks() / 100;

                    inv.setInMiddle(new MakeItem(mineBlock.getMaterial())
                            .setData(mineBlock.getData())
                            .setName(" <r>")
                            .setLore(new ArrayList<>())
                            .addLoreList(
                                    "",
                                    " &7Porcentagem: &f" + NumberUtil.format(mineBlock.getPercentage()) + "&7% (Média: &f" + NumberUtil.formatNumberSimple(amountOfBlocksWirthPercentage) + "&7) ",
                                    "",
                                    " &7Clique com o &fesquerdo &7para adicionar &f2,5&7% ",
                                    " &7Clique com o &fdireito &7para remover &f2,5&7% ",
                                    " &7Clique segurando o &fshift &7para adicionar ou remover &f10&7% ",
                                    "")
                            .build());

                });

        inv.addClickHandler(onClick -> {

            ItemStack currentItem = onClick.getCurrentItem();

            if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.getType().isBlock())
                return;

            if (onClick.getClickedInventory().getType() != InventoryType.PLAYER && onClick.getSlot() != 53) {

                MineBlock mineBlock = null;
                for (MineBlock mineBlock1 : mine.getMineBlocks()) {
                    if (mineBlock1.getMaterial() == currentItem.getType() && mineBlock1.getData() == currentItem.getData().getData())
                        mineBlock = mineBlock1;
                }

                if (mineBlock == null) {
                    plugin.getMessageManager().sendMessage(player, "Not contains block",
                            "{name}", mine.getName());
                    return;
                }

                if (onClick.isLeftClick()) {

                    double increaseAmount = onClick.isShiftClick() ? 10 : 2.5;

                    if (mineBlock.getPercentage() + increaseAmount > 100) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                        return;
                    }

                    mineBlock.setPercentage(mineBlock.getPercentage() + increaseAmount);

                } else if (onClick.isRightClick()) {

                    double increaseAmount = onClick.isShiftClick() ? 10 : 2.5;

                    if (mineBlock.getPercentage() - increaseAmount < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                        return;
                    }

                    mineBlock.setPercentage(mineBlock.getPercentage() - increaseAmount);
                }

                double amountOfBlocksWirthPercentage = mineBlock.getPercentage() * mine.getMaxBlocks() / 100;

                List<String> lore = new LinkedList<>(Arrays.asList("",
                        " &7Porcentagem: &f" + NumberUtil.format(mineBlock.getPercentage()) + "&7% (Média: &f" + NumberUtil.formatNumberSimple(amountOfBlocksWirthPercentage) + "&7) ",
                        "",
                        " &7Clique com o &fesquerdo &7para adicionar &f2,5&7% ",
                        " &7Clique com o &fdireito &7para remover &f2,5&7% ",
                        " &7Clique segurando o &fshift &7para adicionar ou remover &f10&7% ",
                        ""));

                if (mineBlock.getPercentage() <= 0) {
                    lore.addAll(Arrays.asList(
                            "",
                            " &cCaso a porcentagem continue em 0 esse bloco será removido! ",
                            ""));
                }

                inv.updateItem(onClick.getSlot(), new MakeItem(currentItem)
                        .setName(" <r>")
                        .setLore(lore)
                        .build());

            } else if (onClick.getClickedInventory().getType() == InventoryType.PLAYER) {

                MineBlock mineBlock = mine.getMineBlocks().stream()
                        .filter(mineBlockFilter -> mineBlockFilter.getMaterial() == currentItem.getType() && mineBlockFilter.getData() == currentItem.getData().getData())
                        .findFirst().orElse(null);

                if (mineBlock != null) {
                    plugin.getMessageManager().sendMessage(player, "Already contains block",
                            "{name}", mine.getName());
                    return;
                }

                inv.setInMiddle(new MakeItem(currentItem)
                        .setName(" <r>")
                        .setLore(new ArrayList<>())
                        .addLoreList(
                                "",
                                " &7Porcentagem: &f0&7% (Média: &f0&7) ",
                                "",
                                " &7Clique com o &fesquerdo &7para adicionar &f2,5&7% ",
                                " &7Clique com o &fdireito &7para remover &f2,5&7% ",
                                " &7Clique segurando o &fshift &7para adicionar ou remover &f10&7% ",
                                "",
                                " &cCaso a porcentagem continue em 0 esse bloco será removido! ",
                                "")
                        .build());

                mineBlock = new MineBlock(currentItem.getType(), currentItem.getData().getData(), 0);
                mine.getMineBlocks().add(mineBlock);
            }

            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        });

        inv.setItem(53,
                new MakeItem(Material.WOOL)
                        .setData(5)
                        .setName(" <r>")
                        .addLoreList(
                                "",
                                " &7Clique aqui para salvar as alterações na mina &f" + mine.getName() + "&7. ",
                                "",
                                " &7Caso não tenha nenhum bloco ou a porcentagem seja igual a &f0&7, ",
                                " &7A operação será automáticamente cancelada. ",
                                "")
                        .build(), onClick -> {

                    if (mine.getMineBlocks().isEmpty()) {
                        plugin.getMessageManager().sendMessage(player, "Empty blocks",
                                "{name}", mine.getName());

                        plugin.getMineManager().getSetupMap().remove(player.getName());
                        player.closeInventory();
                        return;
                    }

                    double totalPercentage = 0;

                    Iterator<MineBlock> mineBlockIterator = mine.getMineBlocks().iterator();
                    while (mineBlockIterator.hasNext()) {
                        MineBlock mineBlock = mineBlockIterator.next();

                        if (mineBlock.getPercentage() <= 0) {
                            mineBlockIterator.remove();
                            continue;
                        }

                        mineBlock.setMinPercentage(totalPercentage);

                        totalPercentage += mineBlock.getPercentage();

                        mineBlock.setMaxPercentage(totalPercentage);
                    }

                    if (totalPercentage != 100) {
                        plugin.getMessageManager().sendMessage(player, "Incorrect block percentage",
                                "{name}", mine.getName());
                        return;
                    }

                    if (!mine.getMineBlocks().equals(toCheckAtEnd)) {
                        mineService.saveMine(mine, mineService.getMines().size() > 10);
                        mineService.resetMine(mine);
                    }

                    plugin.getMessageManager().sendMessage(player, "Mine changed",
                            "{name}", mine.getName());

                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                    player.closeInventory();
                });

        inv.open(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);

    }

    public void createHologram(Player player, String mineName) {

        if (!mineService.hasMine(mineName)) {
            messageManager.sendMessage(player, "Not created",
                    "{name}", mineName);
            return;
        }

        Mine mine = mineService.getMine(mineName);

        if (mine.getHologram() != null) {
            mine.getHologram().clear();
            mine.getHologram().getHologramLines().forEach(hologramLine -> hologramLine.getArmorStand().remove());
        }

        Hologram hologram = new Hologram(plugin, player.getLocation(), mine.getName().toLowerCase());

        hologram.addLine(" &fMina &d" + mine.getName() + "&f! ");
        hologram.addLine(" &fPorcentagem para resetar &d" + NumberUtil.formatNumber(mine.getCurrentPercentage()) + "% &fde &d" + NumberUtil.formatNumber(mine.getResetPercentage()) + "%&f! ");

        hologram.setRemoveOnDisable(true);
        hologram.setup();

        mine.setHologram(hologram);

        mineService.saveMine(mine, mineService.getMines().size() > 10);

        messageManager.sendMessage(player, "Hologram created",
                "{name}", mine.getName());
    }

    public void hologramTask() {

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {

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

        }, 140, 20);

    }

    public MessageManager getMessageManager() {
        return messageManager;
    }


    public IMineService getMineService() {
        return mineService;
    }


    public Map<String, Mine> getSetupMap() {
        return setupMap;
    }
}
