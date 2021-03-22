package renegade.planetside2;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import renegade.planetside2.data.Outfit;
import renegade.planetside2.data.OutfitPlayer;
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

import static renegade.planetside2.util.Utility.MINUTE;

public enum RenegadeTracker {
    //https://discord.com/oauth2/authorize?client_id=817749037188775936&permissions=8&scope=bot
    //https://discord.com/oauth2/authorize?client_id=817749037188775936&permissions=4294966215&scope=bot
    INSTANCE;

    public static void main(String[] args) {
        try {
            Utility.setStatus(Activity.ActivityType.STREAMING, "Planetside 2", null);
        } catch (Exception ignored){}
        while (true) {
            long started = System.currentTimeMillis();
            long time = INSTANCE.configuration.getScheduleInterval();
            try {
                if (time == -1) break;
                System.out.println("Starting scheduled rank check.");
                boolean success = INSTANCE.calculateRanks(true);
                long duration = System.currentTimeMillis() - started;
                if (success) {
                    System.out.println("Scheduled rank check completed. (" + duration + "ms)");
                    Utility.sleep(time - duration);
                } else {
                    System.out.println("Scheduled rank check failed. Trying again.");
                }
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

    public boolean calculateRanks(boolean updateRanks) {
        HashSet<Long> outfitMembers = new HashSet<>();
        Map<Long, Database.VerifiedData> ps2Discord = database.getPS2DiscordMap();
        List<String> notInDiscord = new ArrayList<>();
        Outfit outfit = Outfit.getR18();
        if (outfit == null) return false;
        Multimap<String, String> renegadeMap = ArrayListMultimap.create();
        for (OutfitPlayer player : outfit.getMembers()) {
            outfitMembers.add(player.getCharacter_id());
            Database.VerifiedData discord = ps2Discord.get(player.getCharacter_id());
            OutfitMember member = manager.getMember(discord, player).orElse(null);
            if ((player.checkForRenegade() || member != null && !member.isRenegade()) && player.hasAny(bananaIds)) {
                if (member != null) member.assignRenegade();
                renegadeMap.put(player.getRank(), player.getActualName());
            }
            if (member != null && updateRanks) member.configureRanks();
            if (member == null && configuration.shouldBeInDiscord(player.getRank())) {
                notInDiscord.add(player.getActualName());
            }
        }

        TextChannel channel = configuration.getCommandChannel();
        EmbedBuilder builder = Utility.embed();
        if (channel != null && !renegadeMap.isEmpty()) {
            String title = "The following players have the renegade camo,\nbut are not renegades in-game.";
            builder.addField(title, "", false);
            renegadeMap.asMap().forEach((rank, names) -> builder.addField(rank, String.join("\n", names), false));
        }
        /*probably not the best idea as no one in the db for now lmao
        if (channel != null && !notInDiscord.isEmpty()) {
            String title = "The following players are members,\nbut are not in discord.";
            builder.addField(title, String.join("\n", notInDiscord), false);
        }*/

        //if (renegadeMap.isEmpty() && notInDiscord.isEmpty() && channel != null) {
        if (renegadeMap.isEmpty() && channel != null) {
            MessageEmbed embed = Utility.embed()
                    .addField("Renegade Check", "All members with camo appear to\n" +
                            "be correctly assigned in-game.", false)
                    .build();
            configuration.getCommandChannel()
                    .sendMessage(embed)
                    .queue();
        } else if (channel != null){
            configuration.getCommandChannel()
                    .sendMessage(builder.build())
                    .queue();
        }

        if (!configuration.shouldStripLeaving() || outfit.getMembers().isEmpty() || outfit.getMembers().size() < 100) return true;
        List<Database.VerifiedData> entries = database.getEntries();
        entries.stream()
                .peek(user->{
                    if (outfitMembers.contains(user.getPs2()) && !user.isMember()) user.setMember(true);
                }).filter(user-> !outfitMembers.contains(user.getPs2()))
                .filter(Database.VerifiedData::isMember)
                .forEach(manager::removeLeavingMember);
        return true;
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
    public void saveConfig() {
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
