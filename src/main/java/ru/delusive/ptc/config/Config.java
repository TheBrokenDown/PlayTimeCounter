package ru.delusive.ptc.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    @Setting(value="global")
    private GlobalParams globalParams = new GlobalParams();
    @Setting(value="DBParams")
    private DBParams DBParams = new DBParams();

    @ConfigSerializable
    public static class GlobalParams{

        @Setting(value="isEnabled", comment = "Should it work?")
        private boolean isEnabled = false;

        public boolean isEnabled() {
            return isEnabled;
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

    public GlobalParams getGlobalParams(){
        return this.globalParams;
    }
    public DBParams getDBParams(){
        return DBParams;
    }

}
