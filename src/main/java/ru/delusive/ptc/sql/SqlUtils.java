package ru.delusive.ptc.sql;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.statistic.Statistics;
import ru.delusive.ptc.Main;
import ru.delusive.ptc.NucleusIntegration;
import ru.delusive.ptc.PlayTimeData;
import ru.delusive.ptc.config.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlUtils {
    private SqlWorker sql;
    private Config.DBParams dbParams;
    private int playTimeTopUserCount;

    public SqlUtils() {
        sql = Main.getInstance().getSqlWorker();
        Config cfg = Main.getInstance().getConfigManager().getConfig();
        dbParams = cfg.getDBParams();
        playTimeTopUserCount = cfg.getGlobalParams().getPlayTimeTopUserCount();
    }

    public int getPlayTime(String username) throws SQLException, IllegalArgumentException {
        String query = String.format("SELECT `%s` FROM `%s` WHERE `%s` = ?",
                dbParams.getPlaytimeColumn(),
                dbParams.getTableName(),
                dbParams.getUsernameColumn());
        ResultSet rs = sql.executeQuery(query, username);
        int playtime = 0;
        if(rs.next()) {
            playtime = rs.getInt(1);
        } else {
            throw new IllegalArgumentException("Player not found!");
        }
        return playtime;
    }


    public Map<Integer, PlayTimeData> getPlayTimeTop() {
        String query = String.format("SELECT `%s`, `%s` FROM `%s` ORDER BY `%s` DESC LIMIT %s",
                dbParams.getUsernameColumn(),
                dbParams.getPlaytimeColumn(),
                dbParams.getTableName(),
                dbParams.getPlaytimeColumn(),
                playTimeTopUserCount);
        ResultSet res = sql.executeQuery(query);
        Map<Integer, PlayTimeData> map = new HashMap<>();
        try {
            for(int i = 1; res.next(); i++) {
                PlayTimeData playTimeData = new PlayTimeData(res.getString(1), res.getInt(2));
                map.put(i, playTimeData);
            }
        } catch (SQLException e) {e.printStackTrace(); }
        return map;
    }

    //Tell me, is it ok to allow bulky methods like this to exist? Maybe i should split it?
    public void updatePlayTime() throws SQLException {

        boolean isNucleusSuppEnabled = Main.getInstance().isNucleusSuppEnabled();
        NucleusIntegration nucleus = Main.getInstance().getNucleus();
        //NucleusAFKService afkService = Main.getInstance().getAfkService();

        HashMap<String, Player> players = new HashMap<>();
        HashMap<String, Player> hasPlayedBefore = new HashMap<>();
        HashMap<String, Player> notPlayedBefore;// = new HashSet<>();
        //Adding player names in collection
        if(isNucleusSuppEnabled) {
            for(Player p : Sponge.getServer().getOnlinePlayers()) if(!nucleus.getAfkService().isAFK(p)) players.put(p.getName(), p);
        } else {
            for(Player p : Sponge.getServer().getOnlinePlayers()) players.put(p.getName(), p);
        }
        if(players.size() == 0) return;
        notPlayedBefore = new HashMap<>(players);
        //Getting the names of the players who have played before
        String query = String.format("SELECT `%s` FROM `%s` WHERE `%s` IN (%s)",
                dbParams.getUsernameColumn(),
                dbParams.getTableName(),
                dbParams.getUsernameColumn(),
                toQuestionMarks(players.keySet()));
        ResultSet res = sql.executeQuery(query, players.keySet().toArray(new String[0]));
        //Adding them to hasPlayedBefore and removing them from notHasPlayedBefore
        while(res.next()) {
            String name = res.getString(1);
            notPlayedBefore.remove(name);
            hasPlayedBefore.put(name, players.get(name));
        }

        //Adding new players to database
        boolean isUUIDEnabled = !dbParams.getUuidColumn().equalsIgnoreCase("null");
        String stmt;
        if(notPlayedBefore.size() != 0) {
            String[] params;
            if (isUUIDEnabled) {
                stmt = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`) VALUES %s",
                        dbParams.getTableName(),
                        dbParams.getUsernameColumn(),
                        dbParams.getUuidColumn(),
                        dbParams.getPlaytimeColumn(),
                        toQuestionMarks(notPlayedBefore.size(), 3));
                params = new String[notPlayedBefore.size()*3];
                int i = 0;
                for(Map.Entry<String, Player> set : notPlayedBefore.entrySet()) {
                    params[3*i] = set.getKey();
                    params[3*i+1] = set.getValue().getUniqueId().toString();
                    params[3*i+2] = String.valueOf(set.getValue().getStatisticData().get(Statistics.TIME_PLAYED).get()/20/60);
                    i++;
                }
            } else {
                stmt = String.format("INSERT INTO `%s` (`%s`, `%s`) VALUES %s",
                        dbParams.getTableName(),
                        dbParams.getUsernameColumn(),
                        dbParams.getPlaytimeColumn(),
                        toQuestionMarks(notPlayedBefore.size(), 2));
                params = new String[notPlayedBefore.size()*2];
                int i = 0;
                for(Map.Entry<String, Player> set : notPlayedBefore.entrySet()) {
                    params[2*i] = set.getKey();
                    params[2*i+1] = String.valueOf(set.getValue().getStatisticData().get(Statistics.TIME_PLAYED).orElse(0L)/20/60);
                    i++;
                }
            }
            sql.executeUpdate(stmt, params);
        }

        //Updating playing time of players that already exists in database
        if(hasPlayedBefore.size() != 0) {
            if (isNucleusSuppEnabled) {
                stmt = String.format("UPDATE `%s` SET `%s` = `%s` + 1 WHERE `%s` IN (%s)",
                        dbParams.getTableName(),
                        dbParams.getPlaytimeColumn(),
                        dbParams.getPlaytimeColumn(),
                        dbParams.getUsernameColumn(),
                        toQuestionMarks(hasPlayedBefore.keySet()));
                sql.executeUpdate(stmt, hasPlayedBefore.keySet().toArray(new String[0]));
            } else {
                stmt = String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?",
                        dbParams.getTableName(),
                        dbParams.getPlaytimeColumn(),
                        dbParams.getUsernameColumn());
                try(Connection con = sql.getConnection()) {
                    PreparedStatement ps = con.prepareStatement(stmt);
                    for(Player p : hasPlayedBefore.values()) {
                        ps.setString(1, String.valueOf(p.getStatisticData().get(Statistics.TIME_PLAYED).get()/20/60));
                        ps.setString(2, p.getName());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private String toQuestionMarks(Collection<String> collection){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < collection.size(); i++){
            sb.append("?, ");
        }
        return sb.toString().substring(0, sb.toString().length() - 2);
    }

    private String toQuestionMarks(String[] arr){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < arr.length; i++){
            sb.append("?, ");
        }
        return sb.toString().substring(0, sb.toString().length() - 2);
    }

    private String toQuestionMarks(int bracketsCount, int QMPerBrackets){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bracketsCount; i++){
            sb.append("(");
            for(int k = 0; k < QMPerBrackets; k++){
                sb.append("?, ");
            }
            sb.delete(sb.toString().length() - 2, sb.toString().length());
            sb.append("), ");
        }
        return sb.toString().substring(0, sb.toString().length() - 2);
    }
}
