package ru.delusive.ptc.mysql;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import ru.delusive.ptc.MainClass;
import ru.delusive.ptc.config.Config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlWorker {
    private SqlService sql;
    private Config cfg;

    public MysqlWorker(){
        this.cfg = MainClass.getInstance().getConfigManager().getConfig();
    }

    private DataSource getDataSource() throws SQLException {
        sql = Sponge.getServiceManager().provide(SqlService.class).get();
        String alias = sql.getConnectionUrlFromAlias(cfg.getDBParams().getAlias()).orElseThrow(() -> new IllegalArgumentException("JDBC alias not found! Check it in global.conf"));
        return sql.getDataSource(alias);
    }

    void executeUpdate(String str, String... args) {
        try(Connection con = getDataSource().getConnection()){
            PreparedStatement stmt = con.prepareStatement(str);
            int i = 1;
            for(String arg : args){
                stmt.setString(i, arg);
                i++;
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ResultSet executeQuery(String str, String... args) {
        try(Connection con = getDataSource().getConnection()){
            PreparedStatement stmt = con.prepareStatement(str);
            int i = 1;
            for(String arg : args){
                stmt.setString(i, arg);
                i++;
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createTable(){
        try(Connection con = getDataSource().getConnection()){
            boolean isUuid = !cfg.getDBParams().getUuidColumn().equalsIgnoreCase("null");
            String sql = String.format(
                    "CREATE TABLE IF NOT EXISTS`%s` (\n" +
                    "`%s` CHAR(30) NOT NULL,\n" +
                    (isUuid ? "`%s` CHAR(40) NULL,\n" : "%s") +
                    "`%s` INT NOT NULL\n" +
                    ")", cfg.getDBParams().getTableName(),
                    cfg.getDBParams().getUsernameColumn(),
                    isUuid ? cfg.getDBParams().getUuidColumn() : "",
                    cfg.getDBParams().getPlaytimeColumn());
            con.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean canConnect(){
        try(Connection con = getDataSource().getConnection()) {
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
