package renegade.planetside2;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import renegade.planetside2.data.OutfitPlayer;
import renegade.planetside2.data.Outfit;
import renegade.planetside2.handlers.DiscordEvents;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.storage.Database;
import renegade.planetside2.tracker.OutfitMember;
import renegade.planetside2.tracker.UserManager;
import renegade.planetside2.util.Utility;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static renegade.planetside2.util.Utility.*;

public enum RenegadeTracker {
    //https://discord.com/oauth2/authorize?client_id=817749037188775936&permissions=8&scope=bot
    INSTANCE;

    public static void main(String[] args) {
        try {
            Utility.setStatus(Activity.ActivityType.STREAMING, "Planetside 2", null);
        } catch (Exception ignored){}
        while (true) {
            try {
                long time = INSTANCE.configuration.getScheduleInterval();
                if (time == -1) break;
                System.out.println("Starting scheduled rank check.");
                INSTANCE.calculateRanks(true);
                System.out.println("Scheduled rank check completed.");
                Utility.sleep(time);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

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
        while (jda == null) {
            try {
                this.jda = getJda(intents, manager);
            } catch (InterruptedException | LoginException e) {
                e.printStackTrace();
                Utility.sleep(MINUTE);
            }
        }
    }

    public JDA getJda(){
         return jda;
    }

    public void updateBananaIds(){
        bananaIds = configuration.getBananaIds();
    }

    public List<Long> getAcceptableCamos() {
         return bananaIds;
    }

    public void calculateRanks(boolean updateRanks) {
        HashSet<Long> outfitMembers = new HashSet<>();
        Map<Long, Long> ps2Discord = database.getPS2DiscordMap();
        List<OutfitPlayer> members = Outfit.getR18().getMembers();
        Multimap<String, String> renegadeMap = ArrayListMultimap.create();
        for (OutfitPlayer player : members) {
            outfitMembers.add(player.getCharacter_id());
            long discord = ps2Discord.getOrDefault(player.getCharacter_id(), -1L);
            OutfitMember member = manager.getMember(discord, player).orElse(null);
            if ((player.checkForRenegade() || member != null && !member.isRenegade()) && player.hasAny(bananaIds)) {
                if (member != null) member.assignRenegade();
                else renegadeMap.put(player.getRank(), player.getActualName());
            }
            if (member != null && updateRanks) member.configureRanks();
        }
        String title = "The following players have the renegade camo,\nbut are not linked to a discord account";
        TextChannel channel = configuration.getCommandChannel();
        if (channel != null) {
            EmbedBuilder builder = Utility.embed()
                    .addField(title, "", false);
            renegadeMap.asMap().forEach((rank, names) -> builder.addField(rank, String.join("\n", names), false));
            configuration.getCommandChannel()
                    .sendMessage(builder.build())
                    .queue();
        }
        database.getEntries().stream()
                .map(Database.VerifiedData::getDiscord)
                .filter(user-> !outfitMembers.contains(user))
                .forEach(manager::removeLeavingMember);
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

    public UserManager getManager() {
         return manager;
    }

    public Database getDatabase() {
         return database;
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
