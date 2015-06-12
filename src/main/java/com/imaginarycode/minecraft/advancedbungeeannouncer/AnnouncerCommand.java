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
        super("announcer");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings)
    {
        if (strings.length < 1)
        {
            about(commandSender);
            return;
        }
        switch (strings[0])
        {
            case "about":
                about(commandSender);
                break;
            case "reload":
                if (commandSender.hasPermission("advancedbungeeannouncer.admin"))
                {
                    AdvancedBungeeAnnouncer.getConfiguration().reloadAnnouncements();
                    AdvancedBungeeAnnouncer.getConfiguration().reloadConfiguration();
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "The plugin was reloaded successfully."));
                } else
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You do not have permission to reload the plugin."));
                }
                break;
            case "create":
                if (commandSender.hasPermission("advancedbungeeannouncer.admin"))
                {
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
                } else
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You do not have permission to create announcements."));
                }
                break;
            case "remove":
                if (commandSender.hasPermission("advancedbungeeannouncer.admin"))
                {
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
                } else
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You do not have permission to remove announcements."));
                }
                break;
            case "list":
                if (commandSender.hasPermission("advancedbungeeannouncer.admin"))
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "Announcements: " + Joiner.on(", ").join(
                            AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().keySet())));
                } else
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You do not have permission to list announcements."));
                }
                break;
            case "info":
                if (commandSender.hasPermission("advancedbungeeannouncer.admin"))
                {
                    if (strings.length < 2)
                    {
                        commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Not enough arguments specified."));
                        commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/announcer info <id>"));
                    }
                    Announcement a = AdvancedBungeeAnnouncer.getConfiguration().getAnnouncements().get(strings[1]);
                    commandSender.sendMessage(TextComponent.fromLegacyText("-------------------------------------"));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "Announcement ID: " + strings[1]));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "Announcement Text:"));
                    commandSender.sendMessage(TextComponent.fromLegacyText(a.getText()));
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "Sent To: " + Joiner.on(", ").join(a.getServers())));
                    commandSender.sendMessage(TextComponent.fromLegacyText("-------------------------------------"));
                } else
                {
                    commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You do not have permission to get information on announcements."));
                }
                break;
            default:
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "/announcer <about|reload|create|remove|{set,add,remove}line|list|info>"));
        }
    }

    private void about(CommandSender cs)
    {
        cs.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "AdvancedBungeeAnnouncer " + AdvancedBungeeAnnouncer.getPlugin().getDescription().getVersion() + " by tuxed"));
        cs.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "This plugin is freely redistributable under the terms of the WTFPL, see http://www.wtfpl.net for more details."));
    }
}
