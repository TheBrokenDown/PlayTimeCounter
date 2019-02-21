package ru.delusive.ptc.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
    @Setting(value="global")
    private GlobalParams globalParams = new GlobalParams();
    @Setting
    private DBParams DBParams = new DBParams();
    @Setting
    private Messages messages = new Messages();

    @ConfigSerializable
    public static class GlobalParams {
        @Setting(comment = "Should it work?")
        private boolean isEnabled = false;
        @Setting(comment = "Should plugin count player's playing time while he is afk? (Using Nucleus AFK API)\n" +
                "If you will change it from false to true, keep in mind that plugin will change all playing time values to values from statistic (not a bug but a feature)")
        private boolean countAFKTime = true;
        @Setting(comment = "How many users should be displayed in playing time top?")
        private int playTimeTopUserCount = 5;

        public boolean isEnabled() {
            return isEnabled;
        }

        public boolean isCountAFKTime() {
            return countAFKTime;
        }

        public int getPlayTimeTopUserCount() {
            return playTimeTopUserCount;
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
        private String errorPluginDisabled = "&cPlugin disabled!";
        @Setting
        private String errorPlayerNotFound = "&cPlayer with specified name not found!";
        @Setting
        private String errorSql = "&cUnexpected error. Please report this to the admin!";
        @Setting
        private String playtimeFormatSelf = "&aYou played for &b%hours% &ahours and &b%minutes% &amins.";
        @Setting
        private String playtimeFormatOther = "&aPlayer &b%player% &aplayed for &b%hours% &ahours and &b%minutes% &amins.";
        @Setting
        private String playTimeTopHeader = "&a==========| TOP |==========";
        @Setting
        private String playTimeTopLineFormat = "&a[&b%position%&a] &b%player% &a- &b%hours%&ah &b%minutes%&am";

        public String getErrorPluginDisabled() {
            return errorPluginDisabled;
        }

        public String getErrorPlayerNotFound() {
            return errorPlayerNotFound;
        }

        public String getErrorSql() {
            return errorSql;
        }

        public String getPlaytimeFormatSelf() {
            return playtimeFormatSelf;
        }

        public String getPlaytimeFormatOther() {
            return playtimeFormatOther;
        }

        public String getPlayTimeTopHeader() {
            return playTimeTopHeader;
        }

        public String getPlayTimeTopLineFormat() {
            return playTimeTopLineFormat;
        }
    }

    public GlobalParams getGlobalParams() {
        return this.globalParams;
    }

    public DBParams getDBParams() {
        return DBParams;
    }

    public Messages getMessages() {
        return messages;
    }
}
