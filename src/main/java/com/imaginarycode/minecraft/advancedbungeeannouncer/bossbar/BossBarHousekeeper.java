package com.imaginarycode.minecraft.advancedbungeeannouncer.bossbar;

import com.imaginarycode.minecraft.advancedbungeeannouncer.AdvancedBungeeAnnouncer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BossBarHousekeeper implements Listener
{
    @Getter
    private static BossBarHousekeeper instance = new BossBarHousekeeper();

    private final ConcurrentMap<UUID, PlayerBossBar> bars = new ConcurrentHashMap<>();

    public void restyleBarsForReload()
    {
        for (PlayerBossBar bar : bars.values())
        {
            bar.setStyling(AdvancedBungeeAnnouncer.getConfiguration().getBarDisplay().getColor(),
                    AdvancedBungeeAnnouncer.getConfiguration().getBarDisplay().getStyle());
        }
    }

    public void setBar(ProxiedPlayer player, String title)
    {
        PlayerBossBar bar = new PlayerBossBar(player, title, AdvancedBungeeAnnouncer.getConfiguration().getBarDisplay().getColor(),
                AdvancedBungeeAnnouncer.getConfiguration().getBarDisplay().getStyle());
        PlayerBossBar bar1 = bars.putIfAbsent(player.getUniqueId(), bar);

        if (bar1 == null)
        {
            bar.send();
        }
        else
        {
            bar1.setTitle(title);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event)
    {
        bars.remove(event.getPlayer().getUniqueId());
    }
}
