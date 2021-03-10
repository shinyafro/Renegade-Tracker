package renegade.planetside2.tracker;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.RestAction;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.data.Player;
import renegade.planetside2.data.Outfit;
import renegade.planetside2.exception.*;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.storage.Database;
import renegade.planetside2.util.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static renegade.planetside2.util.Utility.*;

public class UserManager {
    private final Database database;
    private final RenegadeTracker main;
    private HashMap<String, Player> nameMemberMap;
    private HashMap<Long, Player> longMemberMap;
    private long lastUpdated = 0;

    public UserManager(Database database, RenegadeTracker main){
        this.database = database;
        this.main = main;
    }

    @SubscribeEvent
    public void linkAccount(User source, User target, String input) {
        try {
            String username = input.toLowerCase(Locale.ENGLISH);
            JDA jda = RenegadeTracker.INSTANCE.getJda();
            Configuration cfg = RenegadeTracker.INSTANCE.getConfig();
            linkAccount(target, username);
            source.openPrivateChannel()
                    .flatMap(ch->ch.sendMessage(embed("Success","You have been successfully added to the database.")))
                    .queue();
            TextChannel command = cfg.getCommandChannel();
            String adminNotification = String.format("The user %s has been linked to %s.", target.getAsMention(), input);
            command.sendMessage(embed("Verification", adminNotification)).queue();
            Guild guild = cfg.getGuild(jda);
            guild.retrieveMember(target)
                    .submit()
                    .thenApply(m->guild.modifyNickname(m, input))
                    .thenCompose(RestAction::submit);
        } catch (AlreadyLinkedException e) {
            source.openPrivateChannel()
                    .flatMap(ch->ch.sendMessage(embed("Error", "We have detected your discord account is already in the database. \n" +
                            "If you feel this is in error, please contact an administrator immediately.")))
                    .queue();
        } catch (UsernameTakenException e) {
            source.openPrivateChannel()
                    .flatMap(ch->ch.sendMessage(embed("Error","We have detected your planetside character is already in the database.\n " +
                            "If you feel this is in error, please contact an administrator immediately.")))
                    .queue();
        } catch (UsernameNotInOutfitException e){
            source.openPrivateChannel()
                    .flatMap(ch->ch.sendMessage(embed("Error","We have detected your character is not in the outfit. \n" +
                            "Take note, that it does take up to 10 minutes to update the member list. \n" +
                            "If you feel this is in error, please contact an administrator immediately.")))
                    .queue();
        } catch (LinkException e) {
            source.openPrivateChannel()
                    .flatMap(ch->ch.sendMessage(embed("Error","An error has occurred while trying to link your account. \n" +
                            "If you feel this is in error, please contact an administrator immediately.")))
                    .queue();
        }
    }

    public Optional<OutfitMember> getMember(long discord, Player player) {
        if (discord < 0) return Optional.empty();
        try {
            JDA jda = RenegadeTracker.INSTANCE.getJda();
            Guild guild = RenegadeTracker.INSTANCE.getConfig().getGuild(jda);
            Member member = guild.retrieveMemberById(discord).complete();
            if (player == null || member == null) return Optional.empty();
            else return Optional.of(new OutfitMember(member, player));
        } catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public OutfitMember getMember(Member member) throws UserNotLinkedException {
        Player p = database.getPlanetside(member.getIdLong())
                .map(getLongMemberMap()::get)
                .orElseThrow(UserNotLinkedException::new);
        return new OutfitMember(member, p);
    }

    public RestAction<OutfitMember> getMember(long discordId) throws UserNotLinkedException {
        Guild guild = main.getConfig().getGuild(main.getJda());
        Player p = database.getPlanetside(discordId)
                .map(getLongMemberMap()::get)
                .orElseThrow(UserNotLinkedException::new);
        return guild.retrieveMemberById(discordId)
                .map(m->new OutfitMember(m, p));
    }

    public Collection<OutfitMember> getLinkedMembers() {
        List<Pair<CompletableFuture<Member>, Player>> memberFutures = database.getEntries().stream()
                .map(data -> {
                    JDA jda = main.getJda();
                    Guild guild = main.getConfig().getGuild(jda);
                    CompletableFuture<Member> user = jda.retrieveUserById(data.discord)
                            .submit()
                            .thenApply(guild::retrieveMember)
                            .thenCompose(RestAction::submit);
                    Player player = longMemberMap.get(data.ps2);
                    return new Pair<>(user, player);
                }).collect(Collectors.toList());

        //Run a second loop to queue all, then retrieve all at once as well.
        return memberFutures.stream()
                .map(p -> {
                    try {
                        return new OutfitMember(p.getKey().get(), p.getValue());
                    } catch (Exception e) {
                        System.out.printf("Failed to retrieve future for %s\n", p.getValue().getActualName());
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Collection<Player> getMembers(){
        return longMemberMap.values();
    }

    private void linkAccount(User user, String name) throws LinkException {
        if (database.getPlanetside(user.getIdLong()).isPresent()){
            throw new AlreadyLinkedException();
        }
        HashMap<String, Player> outfitMembers = getNameMemberMap();
        Player member = outfitMembers.get(name);
        if (member == null) throw new UsernameNotInOutfitException();
        else if (database.getDiscord(member.getCharacter_id()).isPresent()){
            throw new UsernameTakenException();
        }
        try {
            database.createRecord(user.getIdLong(), member.getCharacter_id());
        } catch (Exception e){
            throw new FailedToLinkException(e);
        }
    }

    private HashMap<String, Player> getNameMemberMap() {
        updateMaps();
        return nameMemberMap;
    }

    private HashMap<Long, Player> getLongMemberMap() {
        updateMaps();
        return longMemberMap;
    }

    private void purgeInactive(){
        HashMap<Long, Player> memberHashMap = getLongMemberMap();
        List<Database.VerifiedData> verified = database.getEntries();
        verified.stream()
                .filter(data->!memberHashMap.containsKey(data.ps2))
                .forEach(this::unverifyUser);
    }

    private void unverifyUser(Database.VerifiedData data){
        Guild guild = main.getConfig().getGuild(main.getJda());
        List<Role> roles = Stream.of(0l, 0l)
                .map(guild::getRoleById)
                .collect(Collectors.toList());
        Member m = guild.retrieveMemberById(data.discord).complete();
        guild.modifyMemberRoles(m, new ArrayList<>(), roles).complete();
        database.deleteRecordDiscord(data.discord);
    }

    private void updateMaps(){
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdated < 5000) return;
        lastUpdated = currentTime;

        Outfit r18 = Outfit.getR18();
        if (r18 == null || r18.getMembers().isEmpty()) {
            System.out.println("Failed to fetch outfit. Using old data.");
        } else {
            HashMap<Long, Player> longPlayerMap = new HashMap<>();
            HashMap<String, Player> namePlayerMap = new HashMap<>();
            r18.getMembers().forEach(m -> {
                longPlayerMap.put(m.getCharacter_id(), m);
                namePlayerMap.put(m.getLowerName(), m);
            });
            this.nameMemberMap = namePlayerMap;
            this.longMemberMap = longPlayerMap;
        }
    }


}
