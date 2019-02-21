package ru.delusive.ptc.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import ru.delusive.ptc.Main;
import ru.delusive.ptc.config.Config;

import java.sql.SQLException;


public class PlayTimeCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Config.Messages messages = Main.getInstance().getConfigManager().getConfig().getMessages();
        Task.builder().async().execute(() -> {
            if(!Main.getInstance().isEnabled()) {
                sendMessage(src, messages.getErrorPluginDisabled());
                return;
            }
            if((src instanceof ConsoleSource || src instanceof CommandBlockSource) && !args.hasAny("playerName")) {
                src.sendMessage(Text.of(TextColors.RED, "If you use this command from console or from command block, you must specify player name!"));
                return;
            }
            String username = args.hasAny("playerName") ? (String)args.getOne("playerName").get() : src.getName();
            try {
                int playtime = Main.getInstance().getSqlUtils().getPlayTime(username);
                String msg = !src.getName().equalsIgnoreCase(username) ? messages.getPlaytimeFormatOther() : messages.getPlaytimeFormatSelf();
                msg = msg.replace("%player%", username)
                        .replace("%hours%", String.valueOf(playtime / 60))
                        .replace("%minutes%", String.valueOf(playtime % 60));
                sendMessage(src, msg);
            } catch(SQLException e) {
                sendMessage(src, messages.getErrorSql());
                e.printStackTrace();
            } catch(IllegalArgumentException e) {
                sendMessage(src, messages.getErrorPlayerNotFound());
            }
        }).submit(Main.getInstance().getPlugin());
        return CommandResult.success();
    }

    private void sendMessage(CommandSource src, String msg) {
        src.sendMessage(TextSerializers.formattingCode('&').deserialize(msg));
    }
}
