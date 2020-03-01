# PlayTimeCounter
Sponge plugin that counts players' playing time.

## Setup
Plugin tested with SQLite and MySQL, but may be it works with something another.
1. First of all you should set SQL alias in config/sponge/global.conf. Open it and find section "aliases" in section "sql".
If you want to use MySQL, set alias in that format: "alias_name"="jdbc:mysql://username:password@host/database_name".
If you want to use SQLite, set alias in that format: "alias_name"="jdbc:sqlite:path_to_file.db".
2. Drop .jar file into the mods folder.
3. Restart the server.
4. Configure plugin configuration file and reload it by the command "/ptcreload".

## Commands and perms
* `/playtimecounter [playerName]` - Displays playerName's playing time. If argument isn't specified, displays playing time of command sender.
   * Aliases: `/playtime`, `/ptc`
   * Permissions:
     * `playtimecounter.cmd.playtime.base` - Giving access to the command
     * `playtimecounter.cmd.playtime.others` - Giving possibility to see playing time of other player
     
* `/playtimetop` - Displays top by playing time.
  * Aliases: `/topplaytime`, `/ptctop`
  * Permission: `playtimecounter.cmd.playtimetop.base`

* `/ptcreload` - Reloads configuration file.
  * Permission: `playtimecounter.cmd.reload.base`
