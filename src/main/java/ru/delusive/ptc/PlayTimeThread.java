package ru.delusive.ptc;

import org.spongepowered.api.scheduler.Task;
import ru.delusive.ptc.sql.SqlUtils;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

class PlayTimeThread {

    private Task task;
    private SqlUtils sqlUtils;

    public PlayTimeThread(){
        sqlUtils = MainClass.getInstance().getSqlUtils();
        task = Task.builder().execute(() -> {
            try {
                sqlUtils.updatePlayTime();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).async().delay(1, TimeUnit.MINUTES).interval(1, TimeUnit.MINUTES).name("PlayTimeCounter").submit(MainClass.getInstance().getPlugin());
    }

    Task getTask(){
        return this.task;
    }

}
