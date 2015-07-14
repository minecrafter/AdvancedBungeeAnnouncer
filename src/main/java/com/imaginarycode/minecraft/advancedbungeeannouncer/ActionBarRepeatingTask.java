package com.imaginarycode.minecraft.advancedbungeeannouncer;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ActionBarRepeatingTask implements Runnable {
    private int runs;
    private final Map<String, BaseComponent[]> announcements;
    private final int maxRuns;
    private ScheduledTask task;

    public void start() {
        if (maxRuns <= 1)
        {
            run();
        } else
        {
            ProxyServer.getInstance().getScheduler().schedule(AdvancedBungeeAnnouncer.getPlugin(), this, 0, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void run() {
        for (Map.Entry<String, BaseComponent[]> entry : announcements.entrySet())
        {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(entry.getKey());

            if (player != null)
            {
                player.sendMessage(ChatMessageType.ACTION_BAR, entry.getValue());
            }
        }

        runs++;
        if (runs >= maxRuns && task != null)
        {
            task.cancel();
        }
    }
}
