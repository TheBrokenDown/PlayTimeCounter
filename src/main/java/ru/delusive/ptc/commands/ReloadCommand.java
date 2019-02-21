package ru.delusive.ptc.commands;

import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import ru.delusive.ptc.Main;

import java.io.IOException;

public class ReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Task.builder().async().execute(() -> {
            try {
                Main.getInstance().reloadConfig();
            } catch (ObjectMappingException | IOException e) {
                e.printStackTrace();
                src.sendMessage(Text.of(TextColors.RED, "Something went wrong. Error was printed to the console."));
                return;
            }
            src.sendMessage(Text.of(TextColors.GREEN, "Config reloaded successfully!"));
        }).submit(Main.getInstance().getPlugin());
        return CommandResult.success();
    }
}
