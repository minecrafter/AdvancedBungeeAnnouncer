# AdvancedBungeeAnnouncer

**AdvancedBungeeAnnouncer** is an BungeeCord plugin that does one thing - send announcements. However, it offers more fine-grained control.

## Features

 * Multi-server support - no more plugins on **all** of your servers with awkward configuration!
 * **Simple to use** - drop it in your BungeeCord plugin directory and start it. It's not rocket science.
 * Plays nice with BungeeCord, using its interfaces.
 * Coded for [a large minigames server](http://thechunk.net) - perfect for all servers, small and large!
 * Offers random selection of announcements,
 * Supports formatting codes in messages in a Essentials-like format

## Comparison

 * [BungeeAnnouncer](https://github.com/Favorlock/BungeeAnnouncer)
  * uses a Java Timer instead of BungeeCord's scheduler
  * [reimplements ChatColor.translateAlternativeColorCodes()](https://github.com/Favorlock/BungeeAnnouncer/blob/master/src/main/java/com/gmail/favorlock/bungeeannouncer/utils/FontFormat.java)
  * does not allow use of randomized announcements
  * does not have multi-server support
