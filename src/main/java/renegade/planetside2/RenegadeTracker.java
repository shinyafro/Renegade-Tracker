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
import renegade.planetside2.data.Item;
import renegade.planetside2.data.Member;
import renegade.planetside2.data.Outfit;
import renegade.planetside2.handlers.DiscordEvents;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public enum RenegadeTracker {
    INSTANCE;

    public static void main(String[] args) { }
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;

    private JDA jda;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode node;
    private Configuration configuration;
    private List<Long> bananaIds;

     RenegadeTracker(){
         Collection<GatewayIntent> intents = Arrays.asList(
                 GatewayIntent.GUILD_MEMBERS,
                 GatewayIntent.GUILD_MESSAGES
         );
         loader =  HoconConfigurationLoader.builder()
                .setPath(new File("config.hocon").toPath())
                .setDefaultOptions(ConfigurationOptions.defaults().setShouldCopyDefaults(true))
                .build();
        loadConfig();
        updateBananaIds();
        while (true) {
            try {
                if (jda == null) this.jda = getJda(intents);
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

    public void updateBananaIds(){
        bananaIds = configuration.getBananaIds();
    }

    public void calculateRanks(){
        List<Member> members = Outfit.getR18().getMembers();
        for (Member member : members){
            //todo check for discord role instead as it is more reliable.
            //if (member.belowRank(configuration.getInGameRenegade())){
                HashSet<Long> unlocks = member.getItems();
                boolean banana = bananaIds.stream().anyMatch(unlocks::contains);
                if (banana) System.out.println(member.getActualName() + " is a banana!");
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

    private JDA getJda(Collection<GatewayIntent> intents) throws InterruptedException, LoginException {
        loadConfig();
        JDA jda = JDABuilder.createDefault(configuration.getJdaToken(), intents)
                .setEventManager(new AnnotatedEventManager())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();
        jda.awaitStatus(JDA.Status.CONNECTED, JDA.Status.FAILED_TO_LOGIN);
        jda.addEventListener(new DiscordEvents());
        return jda;
    }
}
