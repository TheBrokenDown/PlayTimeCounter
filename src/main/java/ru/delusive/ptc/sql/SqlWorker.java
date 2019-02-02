package ru.delusive.ptc.sql;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import ru.delusive.ptc.Main;
import ru.delusive.ptc.config.Config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlWorker {
    private Config.DBParams dbParams;

    public SqlWorker() {
        dbParams = Main.getInstance().getConfigManager().getConfig().getDBParams();
    }

    private DataSource getDataSource() throws SQLException {
        SqlService sql = Sponge.getServiceManager().provide(SqlService.class).get();
        String alias = sql.getConnectionUrlFromAlias(dbParams.getAlias()).orElseThrow(() -> new IllegalArgumentException("JDBC alias not found! Check it in global.conf"));
        return sql.getDataSource(alias);
    }

    void executeUpdate(String str, String... args) {
        try(Connection con = getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(str);
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i + 1, args[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ResultSet executeQuery(String str, String... args) {
        try(Connection con = getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(str);
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i + 1, args[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createTable() {
        try(Connection con = getDataSource().getConnection()) {
            boolean isUuid = !dbParams.getUuidColumn().equalsIgnoreCase("null");
            String sql = String.format(
                    "CREATE TABLE IF NOT EXISTS`%s` (\n" +
                    "`%s` CHAR(30) NOT NULL,\n" +
                    (isUuid ? "`%s` CHAR(40) NULL,\n" : "%s") +
                    "`%s` INT NOT NULL\n" +
                    ")", dbParams.getTableName(),
                    dbParams.getUsernameColumn(),
                    isUuid ? dbParams.getUuidColumn() : "",
                    dbParams.getPlaytimeColumn());
            con.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean canConnect() {
        try(Connection con = getDataSource().getConnection()) {
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
