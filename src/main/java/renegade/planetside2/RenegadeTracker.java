package renegade.planetside2;

import com.google.common.reflect.TypeToken;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import renegade.planetside2.data.Player;
import renegade.planetside2.data.Outfit;
import renegade.planetside2.handlers.DiscordEvents;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.storage.Database;
import renegade.planetside2.tracker.UserManager;
import renegade.planetside2.util.Pair;
import renegade.planetside2.util.Utility;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public enum RenegadeTracker {
    //https://discord.com/oauth2/authorize?client_id=817749037188775936&permissions=8&scope=bot
    INSTANCE;

    public static void main(String[] args) { }
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final Database database;
    private CommentedConfigurationNode node;
    private Configuration configuration;
    private List<Long> bananaIds;
    private UserManager manager;
    private JDA jda;

     RenegadeTracker(){
         Collection<GatewayIntent> intents = Arrays.asList(
                 GatewayIntent.GUILD_MEMBERS,
                 GatewayIntent.GUILD_MESSAGES,
                 GatewayIntent.DIRECT_MESSAGES
         );
         loader =  HoconConfigurationLoader.builder()
                .setPath(new File("config.hocon").toPath())
                .setDefaultOptions(ConfigurationOptions.defaults().setShouldCopyDefaults(true))
                .build();
        loadConfig();
        updateBananaIds();
        database = Database.INSTANCE;
        manager = new UserManager(database, this);
        while (true) {
            try {
                if (jda == null) this.jda = getJda(intents, manager);
                calculateRanks();
                Utility.sleep(HOUR);
            } catch (InterruptedException | LoginException e) {
                e.printStackTrace();
                Utility.sleep(MINUTE);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public JDA getJda(){
         return jda;
    }

    public void updateBananaIds(){
        bananaIds = configuration.getBananaIds();
    }

    public void calculateRanks(){
        List<Player> members = Outfit.getR18().getMembers();
        for (Player member : members){
            //todo check for discord role instead as it is more reliable.
            //if (member.belowRank(configuration.getInGameRenegade())){
                HashSet<Long> unlocks = member.getItems();
                boolean banana = bananaIds.stream().anyMatch(unlocks::contains);
                if (banana){
                    long discordId = database.getDiscord(member.getCharacter_id()).orElse(-1L);
                    if (discordId > 0) System.out.println("DiscordID::" + discordId);
                    System.out.println(member.getActualName() + " is a renegade");
                }
            //}
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void loadConfig() {
        try {
            if (configuration == null) configuration = new Configuration();
            ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
            node = loader.load(options);
            configuration = node.getValue(TypeToken.of(Configuration.class), configuration);
            loader.save(node);
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void saveConfig(){
        try {
            if (node == null) return;
            loader.save(node.setValue(TypeToken.of(Configuration.class), configuration));
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

    public Configuration getConfig(){
        return configuration;
    }

    private JDA getJda(Collection<GatewayIntent> intents, UserManager manager) throws InterruptedException, LoginException {
        loadConfig();
        JDA jda = JDABuilder.createDefault(configuration.getJdaToken(), intents)
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(new DiscordEvents(manager))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        jda.awaitStatus(JDA.Status.CONNECTED, JDA.Status.FAILED_TO_LOGIN);
        return jda;
    }
}
