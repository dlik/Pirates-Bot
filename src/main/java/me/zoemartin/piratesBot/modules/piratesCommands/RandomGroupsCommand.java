package me.zoemartin.piratesBot.modules.piratesCommands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class RandomGroupsCommand implements GuildCommand {
    @Override
    public String name() {
        return "random-groups";
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MODERATOR;
    }

    @Override
    public String usage() {
        return "random-groups <from-voice-channel> <group-voice-channels-1> <group-voice-channel-2> [<group-voice-channels...>]";
    }

    @Override
    public String description() {
        return "Distribute users to random voice channels with an equal group sizes.";
    }

    @Override
    public Collection<Permission> required() {
        return Arrays.asList(Permission.VOICE_MOVE_OTHERS);
    }

    @Override
    public void run(User executor, MessageChannel channel, List<String> args, Message original, String invoked) {
        Guild guild = ((GuildChannel) channel).getGuild();

        // there need to be at least 3 voice channels referenced:
        // 1 from-channel + at least 2 group-channels
        Check.check(args.size() >= 2, CommandArgumentException::new);
        args.forEach(arg -> {
            Check.check(isValidVoiceChannelReference(arg), CommandArgumentException::new);
        });

        VoiceChannel fromChannel = findVoiceChannel(guild, executor, args.get(0));
        VoiceChannel[] toChannels = args.subList(1, args.size()).stream()
            .map(reference -> findVoiceChannel(guild, executor, reference))
            .distinct()
            .toArray(VoiceChannel[]::new);

        Check.check(toChannels.length >= 2, () -> new ReplyError("Please specify at least two different group voice-channels!"));
        
        Map<Member, VoiceChannel> distribution = distributeUsers(fromChannel, toChannels);

        distribution.entrySet()
            .forEach(entry -> guild.moveVoiceMember(entry.getKey(), entry.getValue()));
        
        String reply = String.format("Distributed %s users from the %s channel to these channels:\n • %s",
            distribution.entrySet().size(), fromChannel.getName(), String.join("\n • ", Arrays.stream(toChannels).map(VoiceChannel::getName).collect(Collectors.toList())));
        channel.sendMessage(reply).submit();
    }

    private boolean isValidVoiceChannelReference(String reference) {
        return reference.equalsIgnoreCase("here") || Parser.Channel.isParsable(reference);
    }

    private VoiceChannel findVoiceChannel(Guild guild, User executor, String reference) {
        if (reference.equalsIgnoreCase("here")) {
            GuildVoiceState voice = guild.getMember(executor).getVoiceState();
            Check.check(voice.inVoiceChannel(), () -> new ReplyError("You can't use `here` if you're not in a voice channel!"));
            return voice.getChannel();
        } else {
            VoiceChannel channel = guild.getVoiceChannelById(Parser.Channel.parse(reference));
            Check.check(channel != null, () -> new ReplyError("Can't find a voice channel with the ID `%s`!", reference));
            return channel;
        }
    }

    /**
     * Creates a user-channel map for distributing all users (excluding bots) in
     * one channel to an array of group channels. The resulting distribution
     * aims for similar group sizes (group sizes vary at most by 1).
     * @param from the channel to distribute users from.
     * @param to the group channels to distribute users to.
     * @return a map that shows which user needs to be moved in which channel.
     */
    public static Map<Member, VoiceChannel> distributeUsers(VoiceChannel from, VoiceChannel[] to) {
        Member[] membersToDistribute = from.getMembers().stream()
            .filter(member -> !member.getUser().isBot())
            .toArray(Member[]::new);
        
        Map<Member, VoiceChannel> memberChannels = new HashMap<>();

        for (int i = 0; i < membersToDistribute.length; i++) {
            Member member = membersToDistribute[i];
            VoiceChannel target = to[i % to.length];
            memberChannels.put(member, target);
        }

        return memberChannels;
    }
}