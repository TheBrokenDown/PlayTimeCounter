package ru.delusive.ptc.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    @Setting(value="global")
    private GlobalParams globalParams = new GlobalParams();
    @Setting(value="DBParams")
    private DBParams DBParams = new DBParams();
    @Setting
    private Messages messages = new Messages();

    @ConfigSerializable
    public static class GlobalParams{

        @Setting(comment = "Should it work?")
        private boolean isEnabled = false;
        @Setting(comment = "Should plugin count player's playing time while he is afk? (Using Nucleus AFK API)\n" +
                "If you will change it from false to true, keep in mind that plugin will change all playing time values to values from statistic (not a bug but a feature)")
        private boolean countAFKTime = true;

        public boolean isEnabled() {
            return isEnabled;
        }
        public boolean isCountAFKTime() {
            return countAFKTime;
        }

    }

    @ConfigSerializable
    public static class DBParams {

        @Setting(value="dbAliasName", comment = "SQL alias name (from global.conf)")
        private String alias = "playTimeAlias";
        @Setting(value="tableName", comment = "Table name in database")
        private String tableName = "playtime";
        @Setting(value="uuidColumn", comment = "Name of column that contains player uuids (\"null\" to disable)")
        private String uuidColumn = "uuid";
        @Setting(value="usernameColumn", comment = "Name of column that contains player names")
        private String usernameColumn = "username";
        @Setting(value="playtimeColumn", comment = "Name of column that contains player play times")
        private String playtimeColumn = "playtime";

        public String getAlias() {
            return alias;
        }
        public String getTableName() {
            return tableName;
        }
        public String getUuidColumn() {
            return uuidColumn;
        }
        public String getUsernameColumn() {
            return usernameColumn;
        }
        public String getPlaytimeColumn() {
            return playtimeColumn;
        }

    }

    @ConfigSerializable
    public static class Messages {

        @Setting
        private String error_plugin_disabled = "&cPlugin disabled!";
        @Setting
        private String error_player_not_found = "&cPlayer with specified name didn't found!";
        @Setting
        private String error_sql = "&cUnexpected error. Please report this to the admin!";
        @Setting
        private String playtime_format_self = "&aYou played for &b%hours% &ahours and &b%minutes% &amins.";
        @Setting
        private String playtime_format_other = "&aPlayer &b%player% &aplayed for &b%hours% &ahours and &b%minutes% &amins.";

        public String getError_plugin_disabled() {
            return error_plugin_disabled;
        }

        public String getError_player_not_found() {
            return error_player_not_found;
        }

        public String getError_sql() {
            return error_sql;
        }

        public String getPlaytime_format_self() {
            return playtime_format_self;
        }

        public String getPlaytime_format_other() {
            return playtime_format_other;
        }
    }

    public GlobalParams getGlobalParams(){
        return this.globalParams;
    }
    public DBParams getDBParams(){
        return DBParams;
    }
    public Messages getMessages(){
        return messages;
    }
}
