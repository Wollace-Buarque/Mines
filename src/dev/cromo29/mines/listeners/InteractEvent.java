package dev.cromo29.mines.listeners;

import dev.cromo29.durkcore.Inventory.Inv;
import dev.cromo29.durkcore.SpecificUtils.NumberUtil;
import dev.cromo29.durkcore.Util.MakeItem;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.manager.MineManager;
import dev.cromo29.mines.object.Mine;
import dev.cromo29.mines.object.MineBlock;
import dev.cromo29.mines.service.MineServiceImpl;
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

import java.util.ArrayList;

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

        if (!mineManager.getSetupMap().containsKey(player.getName().toLowerCase())) return;

        event.setCancelled(true);

        Mine mine = mineManager.getSetupMap().get(player.getName().toLowerCase());

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

            inventory(player, mine);
        }
    }

    public void inventory(Player player, Mine mine) {

        plugin.getMessageManager().sendMessage(player, "Blocks inventory");

        TXT.runLater(plugin, 40, () -> {

            Inv inv = new Inv(54, "Blocos da mina:");
            inv.setIgnorePlayerInventoryClick(false, true);

            inv.addCloseHandler(onClose -> {

                plugin.getMineManager().getSetupMap().remove(player.getName().toLowerCase());

            });

            inv.addClickHandler(onClick -> {

                ItemStack currentItem = onClick.getCurrentItem();

                if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.getType().isBlock())
                    return;

                if (onClick.getClickedInventory().getType() != InventoryType.PLAYER && onClick.getSlot() != 53) {

                    MineBlock mineBlock = mine.getMineBlockList().stream()
                            .filter(mineBlockFilter -> mineBlockFilter.getMaterial() == currentItem.getType() && mineBlockFilter.getData() == currentItem.getData().getData())
                            .findFirst().orElse(null);

                    if (mineBlock == null) {
                        plugin.getMessageManager().sendMessage(player, "Not contains block",
                                "{name}", mine.getName());
                        return;
                    }

                    if (onClick.isLeftClick()) {

                        if (mineBlock.getPercentage() + 5 > 100) return;

                        mineBlock.setPercentage(mineBlock.getPercentage() + 5);

                    } else if (onClick.isRightClick()) {

                        if (mineBlock.getPercentage() - 5 < 0) return;

                        mineBlock.setPercentage(mineBlock.getPercentage() - 5);
                    }


                    inv.updateItem(onClick.getSlot(), new MakeItem(currentItem)
                            .setName(" <r>")
                            .setLore(new ArrayList<>())
                            .addLoreList(
                                    "",
                                    " &7Porcentagem: &f" + NumberUtil.formatNumberSimple(mineBlock.getPercentage()),
                                    "",
                                    " &7Clique com o botão &fesquerdo &7para adicionar &f5 ",
                                    " &7Clique com o botão &fdireito &7para remover &f5 ",
                                    "")
                            .build());


                } else if (onClick.getClickedInventory().getType() == InventoryType.PLAYER) {

                    MineBlock mineBlock = mine.getMineBlockList().stream()
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
                                    " &7Porcentagem: &f0",
                                    "",
                                    " &7Clique com o botão &fesquerdo &7para adicionar &f5 ",
                                    " &7Clique com o botão &fdireito &7para remover &f5 ",
                                    "")
                            .build());

                    mineBlock = new MineBlock(currentItem.getType(), currentItem.getData().getData(), 0);
                    mine.getMineBlockList().add(mineBlock);
                }

                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

            });

            inv.setItem(53,
                    new MakeItem(Material.WOOL)
                            .setData(5)
                            .setName(" <r>")
                            .addLoreList(
                                    "",
                                    " &7Clique aqui para salvar ",
                                    "")
                            .build(),
                    onClick -> {

                        if (mine.getMineBlockList().isEmpty()) {
                            plugin.getMessageManager().sendMessage(player, "Empty blocks",
                                    "{name}", mine.getName());
                            plugin.getMineManager().getSetupMap().remove(player.getName().toLowerCase());
                            player.closeInventory();
                            return;
                        }

                        double totalPercentage = 0;

                        for (MineBlock mineBlock : mine.getMineBlockList()) {
                           mineBlock.setMinPercentage(totalPercentage);

                           totalPercentage += mineBlock.getPercentage();

                           mineBlock.setMaxPercentage(totalPercentage);
                        }

                        if (totalPercentage != 100) {
                            plugin.getMessageManager().sendMessage(player, "Incorrect block percentage",
                                    "{name}", mine.getName());
                            return;
                        }

                        MineServiceImpl mineService = plugin.getMineService();

                        if (mineService.hasMine(mine.getName())) {
                            plugin.getMessageManager().sendMessage(player, "Already created",
                                    "{name}", mine.getName());
                            plugin.getMineManager().getSetupMap().remove(player.getName().toLowerCase());
                            player.closeInventory();
                            return;
                        }

                        plugin.getMineManager().getSetupMap().remove(player.getName().toLowerCase());

                        mineService.setMine(mine);
                        mineService.saveMine(mine, mineService.getMines().size() > 5);
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
