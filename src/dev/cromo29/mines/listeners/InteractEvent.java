package dev.cromo29.mines.listeners;

import dev.cromo29.durkcore.inventory.Inv;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.managers.MineManager;
import dev.cromo29.mines.objects.Mine;
import dev.cromo29.mines.objects.MineBlock;
import dev.cromo29.mines.service.IMineService;
import dev.cromo29.mines.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InteractEvent implements Listener {

    private final MinePlugin plugin;

    public InteractEvent(MinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void interactAtBlock(PlayerInteractEvent event) {

        if (!event.hasBlock() || !event.hasItem()) return;

        if (!event.getItem().isSimilar(new MakeItem(Material.STONE_PICKAXE)
                .setName(" <7>Criar mina")
                .build())) return;

        MineManager mineManager = plugin.getMineManager();

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        if (!mineManager.getSetupMap().containsKey(player.getName())) return;

        event.setCancelled(true);

        Mine mine = mineManager.getSetupMap().get(player.getName());

        if (mine.getStart() == null) {
            mine.setStart(location);

            plugin.getMessageManager().sendMessage(player, "First location",
                    "{x}", location.getBlockX(),
                    "{y}", location.getBlockY(),
                    "{z}", location.getBlockZ());
        } else {

            if (!location.getWorld().getName().equals(mine.getStart().getWorld().getName())) {
                plugin.getMessageManager().sendMessage(player, "Wrong world",
                        "{world}", mine.getStart().getWorld().getName(),
                        "{playerWorld}", location.getWorld().getName());
                return;
            }

            mine.setEnd(location);

            int size = Utils.getBlocksBetweenLocations(mine.getStart(), mine.getEnd()).size();

            mine.setMaxBlocks(size);

            plugin.getMessageManager().sendMessage(player, "Second location",
                    "{x}", location.getBlockX(),
                    "{y}", location.getBlockY(),
                    "{z}", location.getBlockZ(),
                    "{blocks}", size);

            player.getInventory().remove(new MakeItem(Material.STONE_PICKAXE)
                    .setName(" <7>Criar mina")
                    .build());

            inventory(player, mine);
        }
    }

    public void inventory(Player player, Mine mine) {

        plugin.getMessageManager().sendMessage(player, "Blocks inventory",
                "{name}", mine.getName());

        TXT.runLater(plugin, 40, () -> {

            Inv inv = new Inv(54, "Blocos da mina:");
            inv.setIgnorePlayerInventoryClick(false, true);

            inv.addCloseHandler(onClose -> {

                if (!plugin.getMineManager().getSetupMap().containsKey(onClose.getPlayer().getName())) return;

                plugin.getMineManager().getSetupMap().remove(onClose.getPlayer().getName());

                plugin.getMessageManager().sendMessage(player, "Closed inventory",
                        "{name}", mine.getName());

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
                            .setAmount(1)
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
                                    " &7Clique aqui para salvar a mina &f" + mine.getName() + "&7. ",
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

                            totalPercentage += mineBlock.getPercentage();
                        }

                        if (totalPercentage != 100) {
                            plugin.getMessageManager().sendMessage(player, "Incorrect block percentage",
                                    "{name}", mine.getName());
                            return;
                        }

                        IMineService mineService = plugin.getMineService();

                        if (mineService.hasMine(mine.getName())) {
                            plugin.getMessageManager().sendMessage(player, "Already created",
                                    "{name}", mine.getName());

                            plugin.getMineManager().getSetupMap().remove(player.getName());
                            player.closeInventory();
                            return;
                        }

                        plugin.getMineManager().getSetupMap().remove(player.getName());

                        mineService.setMine(mine);
                        mineService.saveMine(mine, mineService.getMines().size() > 10);
                        mineService.resetMine(mine);

                        plugin.getMessageManager().sendMessage(player, "Mine created",
                                "{name}", mine.getName());

                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
                        player.closeInventory();
                    });

            inv.open(player);
            player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
        });
    }
}
