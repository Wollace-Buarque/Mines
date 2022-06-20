package dev.cromo29.mines.managers;

import dev.cromo29.durkcore.hologram.Hologram;
import dev.cromo29.durkcore.hologram.HologramLine;
import dev.cromo29.durkcore.inventory.Inv;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.util.GetValueFromPlayerChat;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.objects.Mine;
import dev.cromo29.mines.objects.MineBlock;
import dev.cromo29.mines.service.IMineService;
import dev.cromo29.mines.service.MineServiceImpl;
import dev.cromo29.mines.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

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

        ItemStack build = new MakeItem(Material.STONE_PICKAXE)
                .setName(" <7>Criar mina")
                .build();

        if (setupMap.containsKey(player.getName())) {
            messageManager.sendMessage(player, "Creation cancelled");
            setupMap.remove(player.getName());

            player.getInventory().remove(build);
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

        player.getInventory().addItem(build);
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

        if (mine.getHologram() != null) {
            mine.getHologram().clear();
            mine.getHologram().getHologramLines().forEach(hologramLine -> hologramLine.getArmorStand().remove());
        }

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

        String minesText = TXT.parse(TXT.createString(mines.stream().map(Mine::getName).toArray(String[]::new), 0, ", "));

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
        editMine(player, mineName, new ArrayList<>());
    }

    private void editMine(Player player, String mineName, List<MineBlock> mineBlocks) {

        if (!mineService.hasMine(mineName)) {
            messageManager.sendMessage(player, "Not created",
                    "{name}", mineName);
            return;
        }

        Mine mine = mineService.getMine(mineName);

        if (mineBlocks.isEmpty()) mineBlocks.addAll(mine.getMineBlocks());

        List<MineBlock> clonedMineBlocks = mineBlocks.stream().map(MineBlock::clone).collect(Collectors.toList());

        plugin.getMessageManager().sendMessage(player, "Blocks inventory");

        Inv inv = new Inv(54, "Blocos da mina:");
        inv.setIgnorePlayerInventoryClick(false, true);

        mineBlocks.stream()
                .sorted(Comparator.comparing(MineBlock::getPercentage).reversed())
                .forEach(mineBlock -> {

                    double amountOfBlocksWirthPercentage = mineBlock.getPercentage() * mine.getMaxBlocks() / 100;

                    inv.setInMiddle(new MakeItem(mineBlock.getMaterial())
                            .setData(mineBlock.getData())
                            .setName(" <r>")
                            .setLore(new ArrayList<>())
                            .addLoreList(
                                    "",
                                    " &7Porcentagem: &f" + Utils.round(mineBlock.getPercentage()) + "&7% (Média: &f" + NumberUtil.formatNumberSimple(amountOfBlocksWirthPercentage) + "&7) ",
                                    "",
                                    " &7Clique com o &fesquerdo &7para adicionar &f2,5&7% ",
                                    " &7Clique com o &fdireito &7para remover &f2,5&7% ",
                                    " &7Clique segurando o &fshift &7para adicionar um valor customizavel ",
                                    "")
                            .build());

                });

        inv.addClickHandler(onClick -> {
            ItemStack currentItem = onClick.getCurrentItem();

            if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.getType().isBlock())
                return;

            if (onClick.getClickedInventory().getType() != InventoryType.PLAYER && onClick.getSlot() != 53) {

                MineBlock mineBlock = clonedMineBlocks.stream()
                        .filter(mineBlockFilter -> mineBlockFilter.getMaterial() == currentItem.getType() && mineBlockFilter.getData() == currentItem.getData().getData())
                        .findFirst()
                        .orElse(null);

                if (mineBlock == null) {
                    plugin.getMessageManager().sendMessage(player, "Not contains block",
                            "{name}", mine.getName());
                    return;
                }

                double amountToChange = 2.5;

                if (onClick.isShiftClick()) {

                    player.closeInventory();

                    plugin.getMessageManager().sendMessage(player, "Custom percentage",
                            "{name}", mine.getName(),
                            "{percentage}", Utils.round(mineBlock.getPercentage()));

                    GetValueFromPlayerChat.getValueFrom(player, "cancelar", true, onGetValue -> {

                        String valueString = onGetValue.getValueString();

                        if (!NumberUtil.isValidDouble(valueString)) {
                            plugin.getMessageManager().sendMessage(player, "Incorrect percentage",
                                    "{name}", mine.getName());

                            onGetValue.repeatGetValueFrom();
                            return;
                        }

                        mineBlock.setPercentage(NumberUtil.getDouble(valueString));

                        editMine(player, mineName, clonedMineBlocks);

                    }, onCancel -> editMine(player, mineName, clonedMineBlocks));

                } else if (onClick.isLeftClick()) {

                    if (mineBlock.getPercentage() + amountToChange > 100) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                        return;
                    }

                    mineBlock.setPercentage(mineBlock.getPercentage() + amountToChange);

                } else if (onClick.isRightClick()) {

                    if (mineBlock.getPercentage() - amountToChange < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                        return;
                    }

                    mineBlock.setPercentage(mineBlock.getPercentage() - amountToChange);
                }

                double amountOfBlocksWirthPercentage = mineBlock.getPercentage() * mine.getMaxBlocks() / 100;

                List<String> lore = new LinkedList<>(Arrays.asList("",
                        " &7Porcentagem: &f" + Utils.round(mineBlock.getPercentage()) + "&7% (Média: &f" + NumberUtil.formatNumberSimple(amountOfBlocksWirthPercentage) + "&7) ",
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

                MineBlock mineBlock = clonedMineBlocks.stream()
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
                clonedMineBlocks.add(mineBlock);
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

                    if (clonedMineBlocks.isEmpty()) {
                        plugin.getMessageManager().sendMessage(player, "Empty blocks",
                                "{name}", mine.getName());

                        plugin.getMineManager().getSetupMap().remove(player.getName());
                        player.closeInventory();
                        return;
                    }

                    double totalPercentage = 0;

                    Iterator<MineBlock> mineBlockIterator = clonedMineBlocks.iterator();
                    while (mineBlockIterator.hasNext()) {
                        MineBlock mineBlock = mineBlockIterator.next();

                        if (mineBlock.getPercentage() <= 0) {
                            mineBlockIterator.remove();
                            continue;
                        }

                        totalPercentage += mineBlock.getPercentage();
                    }

                    if (totalPercentage != 100) {
                        plugin.getMessageManager().sendMessage(player, "Incorrect block percentage",
                                "{name}", mine.getName(),
                                "{percentage}", totalPercentage);
                        return;
                    }

                    player.closeInventory();

                    boolean hasChanges = mineBlocks.stream()
                            .allMatch(mineBlock -> {
                                MineBlock result = clonedMineBlocks.stream()
                                        .filter(clonedMineBlock -> clonedMineBlock.getMaterial() == mineBlock.getMaterial()
                                                && clonedMineBlock.getData() == mineBlock.getData()
                                                && clonedMineBlock.getPercentage() == mineBlock.getPercentage())
                                        .findFirst()
                                        .orElse(null);

                                return result != null;
                            });

                    if (!hasChanges) {
                        plugin.getMessageManager().sendMessage(player, "Mine not changed",
                                "{name}", mine.getName());
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                        return;
                    }

                    mine.setMineBlocks(clonedMineBlocks);

                    mineService.saveMine(mine, mineService.getMines().size() > 10);
                    mineService.resetMine(mine);

                    plugin.getMessageManager().sendMessage(player, "Mine changed",
                            "{name}", mine.getName());

                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
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

                Hologram hologram = mine.getHologram();
                List<HologramLine> hologramLines = hologram.getHologramLines();

                String line = " &fPorcentagem para resetar &d" + NumberUtil.formatNumber(mine.getCurrentPercentage()) + "% &fde &d" + NumberUtil.formatNumber(mine.getResetPercentage()) + "%&f! ";

                for (HologramLine hologramLine : hologramLines) {

                    if (hologramLine.getText().contains("%")) {
                        hologramLine.setText(line);
                        break;
                    }

                }

            }

        }, 20, 20);

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
