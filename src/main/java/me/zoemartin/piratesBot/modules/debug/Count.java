package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Count implements Command {
    @Override
    public @NotNull Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull String name() {
        return "count";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() == 1 && Parser.Int.isParsable(args.get(0)), CommandArgumentException::new);
        int amount = Parser.Int.parse(args.get(0));

        for (int i = 0; i < amount; i++) {
            channel.sendMessageFormat("%d", i).queue();
        }
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public @NotNull String usage() {
        return "<to>";
    }

    @Override
    public @NotNull String description() {
        return "Counts from 0 to a n-1";
    }
}
