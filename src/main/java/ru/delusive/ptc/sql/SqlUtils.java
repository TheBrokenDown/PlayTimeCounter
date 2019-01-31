package ru.delusive.ptc.sql;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.statistic.Statistics;
import ru.delusive.ptc.MainClass;
import ru.delusive.ptc.NucleusIntegration;
import ru.delusive.ptc.config.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlUtils {

    private SqlWorker sql;
    private Config cfg;

    public SqlUtils(){
        sql = MainClass.getInstance().getSqlWorker();
        cfg = MainClass.getInstance().getConfigManager().getConfig();
    }

    public int getPlayTime(String username) throws SQLException {
        String query = String.format("SELECT `%s` FROM `%s` WHERE `%s` = ? LIMIT 1",
                cfg.getDBParams().getPlaytimeColumn(),
                cfg.getDBParams().getTableName(),
                cfg.getDBParams().getUsernameColumn());
        ResultSet resultSet = sql.executeQuery(query, username);
        int playtime = 0;
        if(resultSet.next()) playtime = resultSet.getInt(1);
        return playtime;
    }

    //Tell me, is it ok to allow bulky methods like this to exist? Maybe i should split it?
    public void updatePlayTime() throws SQLException {

        boolean isNucleusSuppEnabled = MainClass.getInstance().isNucleusSuppEnabled();
        NucleusIntegration nucleus = MainClass.getInstance().getNucleus();
        //NucleusAFKService afkService = MainClass.getInstance().getAfkService();

        HashMap<String, Player> players = new HashMap<>();
        HashMap<String, Player> hasPlayedBefore = new HashMap<>();
        HashMap<String, Player> notPlayedBefore;// = new HashSet<>();
        //Adding player names in collection
        if(isNucleusSuppEnabled){
            for(Player p : Sponge.getServer().getOnlinePlayers()) if(!nucleus.getAfkService().isAFK(p)) players.put(p.getName(), p);
        } else {
            for(Player p : Sponge.getServer().getOnlinePlayers()) players.put(p.getName(), p);
        }
        if(players.size() == 0) return;
        notPlayedBefore = new HashMap<>(players);
        //Getting the names of the players who have played before
        String query = String.format("SELECT `%s` FROM `%s` WHERE `%s` IN (%s)",
                cfg.getDBParams().getUsernameColumn(),
                cfg.getDBParams().getTableName(),
                cfg.getDBParams().getUsernameColumn(),
                toQuestionMarks(players.keySet()));
        ResultSet res = sql.executeQuery(query, players.keySet().toArray(new String[0]));
        //Adding them to hasPlayedBefore and removing them from notHasPlayedBefore
        while(res.next()){
            String name = res.getString(1);
            notPlayedBefore.remove(name);
            hasPlayedBefore.put(name, players.get(name));
        }

        //Adding new players to database
        boolean isUUIDEnabled = !cfg.getDBParams().getUuidColumn().equalsIgnoreCase("null");
        String stmt;
        if(notPlayedBefore.size() != 0) {
            String[] params = new String[notPlayedBefore.size()*3];
            if (isUUIDEnabled) {
                stmt = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`) VALUES %s",
                        cfg.getDBParams().getTableName(),
                        cfg.getDBParams().getUsernameColumn(),
                        cfg.getDBParams().getUuidColumn(),
                        cfg.getDBParams().getPlaytimeColumn(),
                        toQuestionMarks(notPlayedBefore.size(), 3));
                int i = 0;
                for(Map.Entry<String, Player> set : notPlayedBefore.entrySet()){
                    params[3*i] = set.getKey();
                    params[3*i+1] = set.getValue().getUniqueId().toString();
                    params[3*i+2] = String.valueOf(set.getValue().getStatisticData().get(Statistics.TIME_PLAYED).get()/20/60);
                    i++;
                }
            } else {
                stmt = String.format("INSERT INTO `%s` (`%s`, `%s`) VALUES %s",
                        cfg.getDBParams().getTableName(),
                        cfg.getDBParams().getUsernameColumn(),
                        cfg.getDBParams().getPlaytimeColumn(),
                        toQuestionMarks(notPlayedBefore.size(), 2));
                int i = 0;
                for(Map.Entry<String, Player> set : notPlayedBefore.entrySet()){
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
                        cfg.getDBParams().getTableName(),
                        cfg.getDBParams().getPlaytimeColumn(),
                        cfg.getDBParams().getPlaytimeColumn(),
                        cfg.getDBParams().getUsernameColumn(),
                        toQuestionMarks(hasPlayedBefore.keySet()));
                sql.executeUpdate(stmt, hasPlayedBefore.keySet().toArray(new String[0]));
            } else {
                stmt = String.format("UPDATE `%s` SET `%s` = ? WHERE `%s` = ?",
                        cfg.getDBParams().getTableName(),
                        cfg.getDBParams().getPlaytimeColumn(),
                        cfg.getDBParams().getUsernameColumn());
                try(Connection con = sql.getConnection()){
                    PreparedStatement ps = con.prepareStatement(stmt);
                    for(Player p : hasPlayedBefore.values()){
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

    private String toQuestionMarks(int BracketsCount, int QMPerBrackets){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < BracketsCount; i++){
            sb.append("(");
            for(int k = 0; k < QMPerBrackets; k++){
                sb.append("?, ");
            }
            sb.delete(sb.toString().length() - 2, sb.toString().length());
            sb.append("), ");
        }
        return sb.toString().substring(0, sb.toString().length() - 2);
    }

    /*private HashSet<String> generateParams(Collection<String> col, boolean withUUID){
        List<String> list = new ArrayList<>();
        for(String name : col){
            if(!Sponge.getServer().getPlayer(name).isPresent()) {notPresented++; continue;}
            params[3*i] = name;
            params[3*i+1] = Sponge.getServer().getPlayer(name).get().getProfile().getUniqueId().toString();
            params[3*i+2] = "1";
            i++;
        }
    }*/
}
