package me.zoemartin.bot.modules.funCommands;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.GuildCommand;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class Echo implements GuildCommand {
    @Override
    public String name() {
        return "echo";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);

        TextChannel c = original.getTextChannel();

        StringBuilder sb = new StringBuilder();

        if (args.get(0).equals(">>")) {
            Check.check(args.size() > 2 && Parser.Channel.isParsable(args.get(1)),
                CommandArgumentException::new);

            c = original.getGuild().getTextChannelById(Parser.Channel.parse(args.get(1)));
            Check.notNull(c, () -> new ReplyError("Channel '%s' does not exist", args.get(1)));

            args.subList(2, args.size()).forEach(s -> sb.append(s).append(" "));
        } else {
            args.forEach(s -> sb.append(s).append(" "));
        }

        c.sendMessageFormat(sb.toString()).queue();
        original.addReaction("U+2705").queue();
    }

    @Override
    public Permission required() {
        return Permission.MANAGE_SERVER;
    }

    @Override
    public String usage() {
        return "Usage: `echo >> #channel message...`\n or `echo message...`";
    }
}