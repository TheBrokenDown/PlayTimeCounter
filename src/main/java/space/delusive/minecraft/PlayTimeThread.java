package space.delusive.minecraft;

import org.spongepowered.api.scheduler.Task;
import space.delusive.minecraft.sql.SqlUtils;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

class PlayTimeThread {
    private Task task;
    private SqlUtils sqlUtils;

    public PlayTimeThread(){
        sqlUtils = Main.getInstance().getSqlUtils();
        task = Task.builder().execute(() -> {
            try {
                sqlUtils.updatePlayTime();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).async().delay(1, TimeUnit.MINUTES).interval(1, TimeUnit.MINUTES).name("PlayTimeCounter").submit(Main.getInstance().getPlugin());
    }

    Task getTask() {
        return task;
    }

}
