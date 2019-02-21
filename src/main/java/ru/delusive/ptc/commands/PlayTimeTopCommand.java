package ru.delusive.ptc.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import ru.delusive.ptc.Main;
import ru.delusive.ptc.PlayTimeData;
import ru.delusive.ptc.config.Config;

import java.util.Map;

public class PlayTimeTopCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Config.Messages msgs = Main.getInstance().getConfigManager().getConfig().getMessages();
        Task.builder().async().execute(() -> {
            if(!Main.getInstance().isEnabled()) {
                sendMessage(src, msgs.getErrorPluginDisabled());
                return;
            }
            Map<Integer, PlayTimeData> top = Main.getInstance().getSqlUtils().getPlayTimeTop();
            sendMessage(src, msgs.getPlayTimeTopHeader());
            if(!top.isEmpty()) {
                for(int i = 1; i <= top.size(); i++) {
                    PlayTimeData playTimeData = top.get(i);
                    String row = msgs.getPlayTimeTopLineFormat()
                            .replace("%position%", String.valueOf(i))
                            .replace("%player%", playTimeData.getUsername())
                            .replace("%hours%", String.valueOf(playTimeData.getPlayTime() / 60))
                            .replace("%minutes%", String.valueOf(playTimeData.getPlayTime() % 60));
                    sendMessage(src, row);
                }
            } else {
                src.sendMessage(Text.of(TextStyles.ITALIC, TextColors.AQUA, "no data"));
            }
        }).submit(Main.getInstance().getPlugin());
        return CommandResult.success();
    }

    private void sendMessage(CommandSource src, String msg) {
        src.sendMessage(TextSerializers.formattingCode('&').deserialize(msg));
    }
}
