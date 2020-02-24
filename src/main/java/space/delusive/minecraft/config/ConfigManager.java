package space.delusive.minecraft.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;


public class ConfigManager {
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationOptions options;
    private Config cfg;

    public ConfigManager(ConfigurationLoader<CommentedConfigurationNode> loader) throws IOException, ObjectMappingException {
        this.loader = loader;
        options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        update();
    }

    public Config getConfig() {
        return cfg;
    }

    private void update() throws ObjectMappingException, IOException {
        CommentedConfigurationNode node = loader.load(options);
        Config cfg = node.getValue(TypeToken.of(Config.class), new Config());
        loader.save(node);
        this.cfg = cfg;
    }
}
