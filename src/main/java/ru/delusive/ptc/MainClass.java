package ru.delusive.ptc;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import ru.delusive.ptc.config.ConfigManager;
import ru.delusive.ptc.mysql.MysqlUtils;
import ru.delusive.ptc.mysql.MysqlWorker;

import java.io.IOException;

@Plugin(id = MainClass.plugin_id, name = "PlayTimeCounter", description = "Counts players' playtime", authors = {"Delusive"})
public class MainClass {

    static final String plugin_id = "playtimecounter";
    @Inject
    private Logger logger;
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigManager cfgManager;
    private PlayTimeThread playTimeThread;
    private static MainClass instance;
    private MysqlWorker mysqlWorker;
    private MysqlUtils mysqlUtils;
    private PluginContainer plugin;

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException, ObjectMappingException {
        instance = this;
        this.plugin = Sponge.getPluginManager().getPlugin(plugin_id).get();
        init();
    }

    @Listener
    public void onReload(GameReloadEvent e) throws IOException, ObjectMappingException {
        logger.info("Reloading config...");
        if(playTimeThread != null)
            playTimeThread.getTask().cancel();
        init();
        logger.info("Reloaded!");
    }

    private void init() throws IOException, ObjectMappingException {
        initConfig();
        if(!cfgManager.getConfig().getGlobalParams().isEnabled()){
            logger.info("isEnabled is set to false in config file.");
            return;
        }
        this.mysqlWorker = new MysqlWorker();
        this.mysqlUtils = new MysqlUtils();
        if(!mysqlWorker.canConnect()){
            logger.error("An error occurred while connecting to database!");
            return;
        }
        if(this.mysqlWorker.createTable())
            playTimeThread = new PlayTimeThread();
        else
            logger.error("An error occurred while creating the table!");
    }

    private void initConfig() throws IOException, ObjectMappingException {
        this.cfgManager = new ConfigManager(loader);
    }

    public ConfigManager getConfigManager(){
        return this.cfgManager;
    }

    public MysqlWorker getMysqlWorker() {
        return mysqlWorker;
    }

    MysqlUtils getMysqlUtils() {
        return mysqlUtils;
    }

    public static MainClass getInstance(){
        return instance;
    }

    PluginContainer getPlugin(){
        return this.plugin;
    }

}
