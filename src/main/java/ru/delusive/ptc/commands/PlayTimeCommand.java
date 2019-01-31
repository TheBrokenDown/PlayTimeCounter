package ru.delusive.ptc.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import ru.delusive.ptc.MainClass;
import ru.delusive.ptc.config.Config;

import java.sql.SQLException;

public class PlayTimeCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Task.builder().async().execute(() -> {
            Config cfg = MainClass.getInstance().getConfigManager().getConfig();
            if(!MainClass.getInstance().isEnabled()){
                src.sendMessage(TextSerializers.formattingCode('&').deserialize(cfg.getMessages().getError_plugin_disabled()));
                return;
            }
            if((src instanceof ConsoleSource || src instanceof CommandBlockSource) && !args.hasAny("playerName")){
                src.sendMessage(Text.of(TextColors.RED, "If you use this command from console or from command block, you must specify player name!"));
                return;
            }
            String username;
            if(args.hasAny("playerName")){
                UserStorageService storage = Sponge.getServiceManager().provide(UserStorageService.class).get();
                if(!storage.get((String)args.getOne("playerName").get()).isPresent()){
                    src.sendMessage(TextSerializers.formattingCode('&').deserialize(cfg.getMessages().getError_player_not_found()));
                    return;
                }
                username = storage.get(args.getOne("playerName").get().toString()).get().getName();
            } else {
                username = src.getName();
            }
            try {
                int playtime = MainClass.getInstance().getSqlUtils().getPlayTime(username);
                String msg = !src.getName().equalsIgnoreCase(username) ? cfg.getMessages().getPlaytime_format_other() : cfg.getMessages().getPlaytime_format_self();
                msg = msg.replace("%player%", username)
                        .replace("%hours%", String.valueOf(playtime/60))
                        .replace("%minutes%", String.valueOf(playtime-playtime/60*60));
                src.sendMessage(TextSerializers.formattingCode('&').deserialize(msg));
            } catch(SQLException e){
                src.sendMessage(TextSerializers.formattingCode('&').deserialize(cfg.getMessages().getError_sql()));
                e.printStackTrace();
            }
        }).submit(MainClass.getInstance().getPlugin());

        return CommandResult.success();
    }
}
