/**
 * Copyright © 2013 tuxed <write@imaginarycode.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package com.imaginarycode.minecraft.advancedbungeeannouncer;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.imaginarycode.minecraft.advancedbungeeannouncer.bossbar.BossBarHousekeeper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnnouncerCommand extends Command
{
    public AnnouncerCommand()
    {
        super("announcer", "advancedbungeeannouncer.admin");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings)
    {
        if (strings.length < 1)
        {
            strings = new String[]{"help"};
        }
        switch (strings[0])
        {
            case "about":
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "AdvancedBungeeAnnouncer " + AdvancedBungeeAnnouncer.getPlugin().getDescription().getVersion() + " by tuxed"));
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "This plugin is freely redistributable under the terms of the WTFPL, see http://www.wtfpl.net for more details."));
                break;
            case "reload":
                AdvancedBungeeAnnouncer.getConfiguration().reloadAnnouncements();
                AdvancedBungeeAnnouncer.getConfiguration().reloadConfiguration();
                BossBarHousekeeper.getInstance().restyleBarsForReload();
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "The plugin was reloaded successfully."));
                break;
            case "create":
                if (strings.length < 4)
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Not enough arguments specified."));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/announcer create <id> <server(s)> <line 1>"));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "<server(s)> may be 'global' if the message is going to be sent to all servers."));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "<server(s)> may have semicolons to separate server names, like hub;pvp."));
                    return;
                }
                if (AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().containsKey(strings[1]))
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An announcement with this ID already exists."));
                    return;
                }
                String message = Joiner.on(" ").join(Arrays.copyOfRange(strings, 3, strings.length));
                List<String> servers;
                if (strings[2].contains(";"))
                {
                    servers = ImmutableList.copyOf(Splitter.on(";").omitEmptyStrings().split(strings[2]));
                } else
                {
                    servers = Collections.singletonList(strings[2]);
                }
                Announcement announcement = new Announcement(message);
                announcement.getServers().addAll(servers);
                AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().put(strings[1], announcement);
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "New announcement added."));
                break;
            case "remove":
                if (strings.length < 2)
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Not enough arguments specified."));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/announcer remove <id>"));
                    return;
                }
                if (!AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().containsKey(strings[1]))
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "An announcement with this ID does not exist."));
                    return;
                }
                AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().remove(strings[1]);
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Announcement removed."));
                break;
            case "list":
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Announcements: " + Joiner.on(", ").join(
                        AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().keySet())));
                break;
            case "info":
                if (strings.length < 2)
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Not enough arguments specified."));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/announcer info <id>"));
                    return;
                }
                Announcement a = AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().get(strings[1]);
                commandSender.sendMessage(TextComponent.fromLegacyText("-------------------------------------"));
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "Announcement ID: " + strings[1]));
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "Announcement Text:"));
                for (String s : a.getText().split("\n"))
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "- " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', s)));
                }
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "Sent To: " + Joiner.on(", ").join(a.getServers())));
                commandSender.sendMessage(TextComponent.fromLegacyText("-------------------------------------"));
                break;
            default:
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/announcer <about|reload|create|remove|list|info>"));
        }
    }
}
