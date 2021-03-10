package renegade.planetside2.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import renegade.planetside2.RenegadeTracker;
import renegade.planetside2.storage.Configuration;
import renegade.planetside2.tracker.Rank;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Utility {
    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static void sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (Throwable ignored){
            //fuck-off
        }
    }
    @SuppressWarnings("UnstableApiUsage")
    public static <T> T getGsonFromUrl(String url, TypeToken<T> type) {
        try {
            return gson.fromJson(getStringFromUrl(url), type.getType());
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static <T> T getGsonFromUrl(String url, Class<T> type) {
        try {
            String json = getStringFromUrl(url);
            return gson.fromJson(json, type);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static String getStringFromUrl(String url) {
        HttpResponse httpResponse = null;
        try {
            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
            HttpRequest httpRequest = requestFactory.buildGetRequest(new GenericUrl(url));
            httpResponse = httpRequest.execute();
            return httpResponse.parseAsString();
        } catch (Exception exception) {
            try {Thread.sleep(2000);} catch (InterruptedException ignored) {}
            System.out.println(exception.getMessage() + "\nRetrying...");
            return getStringFromUrl(url);
        } finally {
            if (httpResponse != null) try{httpResponse.disconnect();}catch (IOException ignored){};
        }
    }

    public static MessageEmbed embed(String title, String contents){
        return new EmbedBuilder()
                .setTitle("R-18 Renegade Tracker")
                .addField(title, contents, false)
                .setColor(Color.YELLOW)
                .build();
    }

    public static EmbedBuilder embed(){
        return new EmbedBuilder()
                .setTitle("R-18 Renegade Tracker")
                .setColor(Color.YELLOW);
    }

    public static void setRoleIfAbsent(Member member, Rank role){
        try {
            Configuration cfg = RenegadeTracker.INSTANCE.getConfig();
            Guild guild = member.getGuild();
            Role discordRole = cfg.getRole(role);
            if (discordRole == null) return;
            guild.addRoleToMember(member, discordRole).queue();
        } catch (Exception ignored){}
    }

    public static void removeRoleIfPresent(Member member, Rank role) {
            try {
                Configuration cfg = RenegadeTracker.INSTANCE.getConfig();
                Guild guild = member.getGuild();
                Role discordRole = cfg.getRole(role);
                if (discordRole == null) return;
                guild.removeRoleFromMember(member, discordRole).queue();
            } catch (Exception ignored) {}
    }

    public static void sendPermissionError(TextChannel source, Member member){
        sendMessage(source, "<@" + member.getUser().getId() + ">, you do **not** have permission to use this command!", 5);
    }

    public static void sendCommandError(TextChannel source, Member member, String msg){
        sendMessage(source, "<@" + member.getUser().getId() + ">, " + msg, 5);
    }

    public static CompletableFuture<Message> sendMessage(TextChannel src, String message, int delay){
        return src.sendMessage(message).submit()
                .whenComplete((msg, ex)-> msg.delete().queueAfter(delay, TimeUnit.SECONDS));
    }

    public static Optional<Long> parseUserId(List<String> args){
        JDA jda = RenegadeTracker.INSTANCE.getJda();
        Guild guild = RenegadeTracker.INSTANCE.getConfig().getGuild(jda);
        return args.stream()
                .filter(s->s.matches("<?@?!?\\d+>?"))
                .map(s->s.replaceAll("<?@?!?(\\d+)>?", "$1"))
                .filter(s->s.matches("\\d+"))
                .map(Long::valueOf)
                .findFirst();
    }

    public static Optional<CompletableFuture<Member>> parseMember(List<String> args){
        JDA jda = RenegadeTracker.INSTANCE.getJda();
        Guild guild = RenegadeTracker.INSTANCE.getConfig().getGuild(jda);
        return args.stream()
                .filter(s->s.matches("<?@?!?\\d+>?"))
                .map(s->s.replaceAll("<?@?!?(\\d+)>?", "$1"))
                .map(guild::retrieveMemberById)
                .map(RestAction::submit)
                .findFirst();
    }
}
