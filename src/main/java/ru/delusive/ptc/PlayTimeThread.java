package ru.delusive.ptc;

import org.spongepowered.api.scheduler.Task;
import ru.delusive.ptc.mysql.MysqlUtils;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

class PlayTimeThread {

    private Task task;
    private Task.Builder builder;
    private MysqlUtils mu;

    PlayTimeThread(){
         mu = MainClass.getInstance().getMysqlUtils();
        task = Task.builder().execute(() -> {
            try {
                mu.updatePlaytime();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).async().delay(1, TimeUnit.MINUTES).interval(1, TimeUnit.MINUTES).name("PlayTimeCounter").submit(MainClass.getInstance().getPlugin());
    }

    Task getTask(){
        return this.task;
    }

}
