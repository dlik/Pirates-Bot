package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.ConsoleError;
import me.zoemartin.piratesBot.core.interfaces.Command;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Sleep implements Command {
    @Override
    public @NotNull Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull String name() {
        return "sleep";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        channel.sendMessageFormat("Started sleeping").queue();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new ConsoleError(e.getMessage());
        }
        channel.sendMessageFormat("Ended sleeping").queue();
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public @NotNull String description() {
        return "Sends the current thread to sleep";
    }
}
