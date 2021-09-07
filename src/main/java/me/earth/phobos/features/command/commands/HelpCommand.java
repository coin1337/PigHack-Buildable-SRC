package me.earth.phobos.features.command.commands;

import me.earth.phobos.PigHack;
import me.earth.phobos.features.command.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("All commands:");
        for (Command command : PigHack.commandManager.getCommands()) {
            HelpCommand.sendMessage(PigHack.commandManager.getPrefix() + command.getName());
        }
    }
}

