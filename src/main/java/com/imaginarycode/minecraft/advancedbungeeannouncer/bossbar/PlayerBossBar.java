package com.imaginarycode.minecraft.advancedbungeeannouncer.bossbar;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.BossBar;

import java.util.UUID;

public class PlayerBossBar
{
    @Getter
    private final ProxiedPlayer player;
    private final UUID bossBarUuid;
    private String title;
    private BarColor color;
    private BarDivision division;
    @Getter
    private boolean sent = false;

    private BossBar createPacket;
    private BossBar destroyPacket;

    public PlayerBossBar(ProxiedPlayer player, String title, BarColor color, BarDivision division)
    {
        this.player = player;
        this.bossBarUuid = UUID.randomUUID();
        this.title = title;
        this.color = color;
        this.division = division;

        createPackets();
    }

    private void createPackets()
    {
        createPacket = new BossBar(bossBarUuid, 0);
        createPacket.setColor(color.ordinal());
        createPacket.setDivision(division.ordinal());
        createPacket.setHealth(1f);
        createPacket.setTitle(title);

        destroyPacket = new BossBar(bossBarUuid, 1);
    }

    public void setTitle(String title)
    {
        this.title = title;
        createPackets();

        if (sent)
        {
            BossBar updatePacket = new BossBar(bossBarUuid, 3);
            updatePacket.setTitle(title);
            player.unsafe().sendPacket(updatePacket);
        }
    }

    public void setStyling(BarColor color, BarDivision division)
    {
        this.color = color;
        this.division = division;

        createPackets();

        if (sent)
        {
            BossBar updatePacket = new BossBar(bossBarUuid, 4);
            updatePacket.setColor(color.ordinal());
            updatePacket.setDivision(division.ordinal());
            player.unsafe().sendPacket(updatePacket);
        }
    }

    public void send()
    {
        if (!sent)
        {
            player.unsafe().sendPacket(createPacket);
            sent = true;
        }
    }

    public void destroy()
    {
        if (sent)
        {
            player.unsafe().sendPacket(destroyPacket);
            sent = false;
        }
    }
}
