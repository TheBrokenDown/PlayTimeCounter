package ru.delusive.ptc.mysql;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import ru.delusive.ptc.MainClass;
import ru.delusive.ptc.config.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class MysqlUtils {

    private MysqlWorker sql;
    private Config cfg;

    public MysqlUtils(){
        sql = MainClass.getInstance().getMysqlWorker();
        cfg = MainClass.getInstance().getConfigManager().getConfig();
    }

    public void updatePlaytime() throws SQLException {
        HashSet<String> onlinePlayerNames = new HashSet<>();
        HashSet<String> playedBeforePlayerNames = new HashSet<>();
        HashSet<String> notPlayedBeforePlayerNames;

        for(Player p : Sponge.getServer().getOnlinePlayers()) onlinePlayerNames.add(p.getName());
        notPlayedBeforePlayerNames = new HashSet<>(onlinePlayerNames);
        if(onlinePlayerNames.size() == 0) return;

        String stmt = String.format("SELECT `%s` FROM `%s` WHERE `%s` IN (%s)",
                cfg.getDBParams().getUsernameColumn(),
                cfg.getDBParams().getTableName(),
                cfg.getDBParams().getUsernameColumn(),
                toQuestionMarks(onlinePlayerNames));
        ResultSet res = sql.executeQuery(stmt, onlinePlayerNames.toArray(new String[0]));

        while(res.next()){
            playedBeforePlayerNames.add(res.getString(1));
            notPlayedBeforePlayerNames.remove(res.getString(1));
        }
        if(!playedBeforePlayerNames.isEmpty()){
            stmt = String.format("UPDATE `%s` SET `%s` = `%s` + 1 WHERE `%s` IN (%s)",
                    cfg.getDBParams().getTableName(),
                    cfg.getDBParams().getPlaytimeColumn(),
                    cfg.getDBParams().getPlaytimeColumn(),
                    cfg.getDBParams().getUsernameColumn(),
                    toQuestionMarks(playedBeforePlayerNames));
            sql.executeUpdate(stmt, onlinePlayerNames.toArray(new String[0]));
        }
        if(!notPlayedBeforePlayerNames.isEmpty())
        if(!cfg.getDBParams().getUuidColumn().equalsIgnoreCase("null")) {
            String[] params = new String[notPlayedBeforePlayerNames.size() * 3];
            int notPresented = 0;
            int i = 0;
            for(String name : notPlayedBeforePlayerNames){
                if(!Sponge.getServer().getPlayer(name).isPresent()) {notPresented++; continue;}
                params[3*i] = name;
                params[3*i+1] = Sponge.getServer().getPlayer(name).get().getProfile().getUniqueId().toString();
                params[3*i+2] = "1";
                i++;
            }
            stmt = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`) VALUES %s",
                    cfg.getDBParams().getTableName(),
                    cfg.getDBParams().getUsernameColumn(),
                    cfg.getDBParams().getUuidColumn(),
                    cfg.getDBParams().getPlaytimeColumn(),
                    toQuestionMarks(notPlayedBeforePlayerNames.size() - notPresented, 3));
            sql.executeUpdate(stmt, params);
        } else {
            String[] params = new String[notPlayedBeforePlayerNames.size() * 2];
            int i = 0;
            for(String name : notPlayedBeforePlayerNames){
                params[2*i] = name;
                params[2*i+1] = "1";
                i++;
            }
            stmt = String.format("INSERT INTO `%s` (`%s`, `%s`) VALUES %s",
                    cfg.getDBParams().getTableName(),
                    cfg.getDBParams().getUsernameColumn(),
                    cfg.getDBParams().getPlaytimeColumn(),
                    toQuestionMarks(notPlayedBeforePlayerNames.size(), 2));
            sql.executeUpdate(stmt, params);
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
