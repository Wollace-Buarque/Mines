package dev.cromo29.mines.command;

import dev.cromo29.durkcore.API.DurkCommand;
import dev.cromo29.mines.MinePlugin;

import java.util.List;

public class MineCommand extends DurkCommand {

    private final MinePlugin plugin;

    public MineCommand(MinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform() {

        if (isArgsLength(1)) {

            if (isArgAtIgnoreCase(0, "listar", "lista")) {

                plugin.getMineManager().listMines(asPlayer());

            } else sendHelp();

        } else if (isArgsLength(2)) {

            if (isArgAtIgnoreCase(0, "holograma")) {

                plugin.getMineManager().createHologram(asPlayer(), argAt(1));

                return;
            }

            String name = argAt(0);

            if (!isValidDouble(argAt(1))) {
                warnNotValidNumber(argAt(1));
                return;
            }

            double resetPercentage = getDouble(argAt(1));

            if (resetPercentage < 0 || resetPercentage > 100) {
                plugin.getMessageManager().sendMessage(asPlayer(), "Incorrect percentage");
                return;
            }

            plugin.getMineManager().createMine(asPlayer(), name, resetPercentage);

        } else if (isArgsLength(3)) {

            if (isArgAtIgnoreCase(0, "deletar", "remover")) {

                String name = argAt(1);

                if (!argAt(2).equalsIgnoreCase("true") && !argAt(2).equalsIgnoreCase("false")) {
                    plugin.getMessageManager().sendMessage(asPlayer(), "Incorrect boolean");
                    return;
                }

                boolean clear = getBoolean(argAt(2));

                plugin.getMineManager().deleteMine(asPlayer(), name, clear);

            } else sendHelp();

        } else sendHelp();

    }

    @Override
    public boolean canConsolePerform() {
        return false;
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

    private void sendHelp() {
        sendMessages("",
                " <b>⁕ <f>/" + getUsedCommand() + " lista <e>- <7>Lista de minas.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " <nome> <porcentagem> <e>- <7>Criar uma mina com porcentagem para resetar.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " holograma <mina> <e>- <7>Criar um holograma para a mina.",
                "",
                " <b>⁕ <f>/" + getUsedCommand() + " deletar <nome> <true/false> <e>- <7>Deletar mina e limpar blocos.",
                "");
    }
}
