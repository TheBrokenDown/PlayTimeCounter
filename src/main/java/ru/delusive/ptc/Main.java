package ru.delusive.ptc;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import ru.delusive.ptc.commands.PlayTimeCommand;
import ru.delusive.ptc.config.ConfigManager;
import ru.delusive.ptc.sql.SqlUtils;
import ru.delusive.ptc.sql.SqlWorker;

import java.io.IOException;

@Plugin(id = Main.PLUGIN_ID, name = "PlayTimeCounter", description = "Counts players' playtime", authors = {"Delusive"},
dependencies = {@Dependency(id = "nucleus", optional = true)})
public class Main {
    static final String PLUGIN_ID = "playtimecounter";
    @Inject
    private Logger logger;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigManager cfgManager;
    private PlayTimeThread playTimeThread;
    private NucleusIntegration nucleus;
    private SqlWorker sqlWorker;
    private SqlUtils sqlUtils;
    private boolean isNucleusSuppEnabled;
    private PluginContainer plugin;
    private static Main instance;
    private boolean isEnabled;

    @Listener
    public void onServerStart(GameStartedServerEvent e) throws IOException, ObjectMappingException {
        instance = this;
        this.plugin = Sponge.getPluginManager().getPlugin(PLUGIN_ID).get();
        init();
        registerCommands();
    }

    @Listener
    public void onReload(GameReloadEvent e) throws IOException, ObjectMappingException {
        logger.info("Reloading config...");
        if(playTimeThread != null) {
            playTimeThread.getTask().cancel();
        }
        init();
        logger.info("Reloaded!");
    }

    private void init() throws IOException, ObjectMappingException {
        isEnabled = false;
        initConfig();
        if(!cfgManager.getConfig().getGlobalParams().isEnabled()) {
            logger.info("isEnabled is set to false in config file.");
            return;
        }

        sqlWorker = new SqlWorker();
        sqlUtils = new SqlUtils();
        boolean isNucleusInstalled = Sponge.getPluginManager().getPlugin("nucleus").isPresent();
        isNucleusSuppEnabled = isNucleusInstalled && !cfgManager.getConfig().getGlobalParams().isCountAFKTime();

        if(!isNucleusInstalled && !cfgManager.getConfig().getGlobalParams().isCountAFKTime()) {
            logger.warn("isCountAFKTime is set to false, but Nucleus is not installed.");
        }
        if(!sqlWorker.canConnect()) {
            logger.error("An error occurred while connecting to database!");
            return;
        }
        if(sqlWorker.createTable()) {
            playTimeThread = new PlayTimeThread();
        } else {
            logger.error("An error occurred while creating the table!");
            return;
        }
        isEnabled = true;
    }

    private void initConfig() throws IOException, ObjectMappingException {
        this.cfgManager = new ConfigManager(loader);
    }

    private void registerCommands() {
        CommandManager manager = Sponge.getCommandManager();
        CommandSpec playtime = CommandSpec.builder()
                .description(Text.of("Displays player's playtime"))
                .permission("playtimecounter.cmd.playtime.base")
                .executor(new PlayTimeCommand())
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.requiringPermission(
                                        GenericArguments.string(Text.of("playerName")), "playtimecounter.cmd.playtime.others")))
                .build();
        manager.register(plugin, playtime,"playtime", "playtimecounter", "ptc");
    }

    public boolean isNucleusSuppEnabled() {
        return isNucleusSuppEnabled;
    }

    public NucleusIntegration getNucleus() {
        if(isNucleusSuppEnabled) {
            if (nucleus == null) nucleus = new NucleusIntegration();
            return nucleus;
        }
        return null;
    }

    public ConfigManager getConfigManager() {
        return cfgManager;
    }

    public SqlWorker getSqlWorker() {
        return sqlWorker;
    }

    public SqlUtils getSqlUtils() {
        return sqlUtils;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    public static Main getInstance() {
        return instance;
    }

}
