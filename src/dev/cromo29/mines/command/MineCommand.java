package dev.cromo29.mines.command;

import dev.cromo29.durkcore.api.DurkCommand;
import dev.cromo29.mines.MinePlugin;
import dev.cromo29.mines.object.Mine;

import java.util.List;
import java.util.stream.Collectors;

public class MineCommand extends DurkCommand {

    private final MinePlugin plugin;

    public MineCommand(MinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform() {

        if (isArgsLength(1)) {

            if (isArgAtIgnoreCase(0, "listar", "lista")) {

                plugin.getMineManager().listMines(getSender());

            } else sendHelp();

        } else if (isArgsLength(2)) {

            if (isArgAtIgnoreCase(0, "holograma")) {

                if (isConsole()) {
                    sendHelp();
                    return;
                }

                plugin.getMineManager().createHologram(asPlayer(), argAt(1));

                return;
            }

            if (isArgAtIgnoreCase(0, "editar")) {

                if (isConsole()) {
                    sendHelp();
                    return;
                }

                plugin.getMineManager().editMine(asPlayer(), argAt(1));

                return;
            }

            if (isArgAtIgnoreCase(0, "resetar")) {

                plugin.getMineManager().resetMine(getSender(), argAt(1));

                return;
            }

            if (isArgAtIgnoreCase(0, "deletar")) {

                plugin.getMineManager().deleteMine(getSender(), argAt(1), true);

                return;
            }

            if (isConsole()) {
                sendHelp();
                return;
            }

            String name = argAt(0);

            if (!isValidDouble(argAt(1))) {
                warnNotValidNumber(argAt(1));
                return;
            }

            double percentageToReset = getDouble(argAt(1));

            if (percentageToReset < 0 || percentageToReset > 100) {
                plugin.getMessageManager().sendMessage(asPlayer(), "Incorrect percentage");
                return;
            }

            plugin.getMineManager().createMine(asPlayer(), name, percentageToReset);

        } else if (isArgsLength(3)) {

            if (isArgAtIgnoreCase(0, "deletar", "remover")) {

                String name = argAt(1);

                if (!argAt(2).equalsIgnoreCase("true") && !argAt(2).equalsIgnoreCase("false")) {
                    plugin.getMessageManager().sendMessage(asPlayer(), "Incorrect boolean");
                    return;
                }

                boolean clear = getBoolean(argAt(2));

                plugin.getMineManager().deleteMine(getSender(), name, clear);

            } else sendHelp();

        } else sendHelp();

    }

    @Override
    public boolean canConsolePerform() {
        return true;
    }

    @Override
    public String getPermission() {
        return "29Mines.ADM";
    }

    @Override
    public String getCommand() {
        return "mine";
    }

    @Override
    public List<String> getAliases() {
        return getList("mina", "minas");
    }

    @Override
    public List<String> tabComplete() {

        if (isArgsLength(2)) {

            if (!isArgAtIgnoreCase(0, "resetar", "editar", "deletar", "holograma"))
                return getPlayersTabComplete(lastArg(), plugin.getServer().getOnlinePlayers());

            List<String> collect = plugin.getMineService().getMines().stream().map(Mine::getName).collect(Collectors.toList());

            return getTabComplete(lastArg(), collect);
        }

        return getPlayersTabComplete(lastArg(), plugin.getServer().getOnlinePlayers());

    }

    private void sendHelp() {
        sendMessages(
                "",
                " <9>■ <f>/" + getUsedCommand() + " lista <e>- <7>Lista de minas.",
                "",
                " <9>■ <f>/" + getUsedCommand() + " resetar <nome> <e>- <7>Resetar mina.",
                "",
                " <9>■ <f>/" + getUsedCommand() + " editar <nome> <e>- <7>Editar mina.",
                "",
                " <9>■ <f>/" + getUsedCommand() + " <nome> <porcentagem> <e>- <7>Criar uma mina com porcentagem para resetar.",
                "",
                " <9>■ <f>/" + getUsedCommand() + " holograma <mina> <e>- <7>Criar um holograma para a mina.",
                "",
                " <9>■ <f>/" + getUsedCommand() + " deletar <nome> <true/false> <e>- <7>Deletar mina e limpar blocos.",
                "");
    }
}
