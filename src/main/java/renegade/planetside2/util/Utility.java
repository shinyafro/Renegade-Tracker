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
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.io.IOException;

public class Utility {
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
}
