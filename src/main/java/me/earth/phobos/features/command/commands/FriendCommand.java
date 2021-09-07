package me.earth.phobos.features.command.commands;

import me.earth.phobos.PigHack;
import me.earth.phobos.features.command.Command;

import java.util.Map;
import java.util.UUID;

public class FriendCommand
        extends Command {
    public FriendCommand() {
        super("friend", new String[]{"<add/del/name/clear>", "<name>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (PigHack.friendManager.getFriends().isEmpty()) {
                FriendCommand.sendMessage("You currently dont have any friends added.");
            } else {
                FriendCommand.sendMessage("Friends: ");
                for (Map.Entry<String, UUID> entry : PigHack.friendManager.getFriends().entrySet()) {
                    FriendCommand.sendMessage(entry.getKey());
                }
            }
            return;
        }
        if (commands.length == 2) {
            switch (commands[0]) {
                case "reset": {
                    PigHack.friendManager.onLoad();
                    FriendCommand.sendMessage("Friends got reset.");
                    break;
                }
                default: {
                    FriendCommand.sendMessage(commands[0] + (PigHack.friendManager.isFriend(commands[0]) ? " is friended." : " isnt friended."));
                }
            }
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    PigHack.friendManager.addFriend(commands[1]);
                    FriendCommand.sendMessage("\u00a7b" + commands[1] + " has been friended");
                    break;
                }
                case "del": {
                    PigHack.friendManager.removeFriend(commands[1]);
                    FriendCommand.sendMessage("\u00a7c" + commands[1] + " has been unfriended");
                    break;
                }
                default: {
                    FriendCommand.sendMessage("\u00a7cBad Command, try: friend <add/del/name> <name>.");
                }
            }
        }
    }
}

