package renegade.planetside2.storage;

import renegade.planetside2.util.Utility;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("SqlResolve")
public enum Database {
    INSTANCE;
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:./RenegadeTracker";
    private static final String USER = "";
    private static final String PASS = "";

    Database(){
        try {
            Class.forName(JDBC_DRIVER);
            initTable();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initTable(){
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()){
            String sql = "CREATE TABLE IF NOT EXISTS `Verification` (" +
                    " `DiscordId` BIGINT NOT NULL, " +
                    " `PlanetsideId` BIGINT NOT NULL, " +
                    " `Verified` TIMESTAMP NOT NULL DEFAULT current_timestamp(), " +
                    " `Member` BIT NOT NULL DEFAULT TRUE, " +
                    " `Assign` BIT NOT NULL DEFAULT TRUE, " +
                    " `Router` BIT NOT NULL DEFAULT FALSE, " +
                    " `Renegade` BIT NOT NULL DEFAULT FALSE, " +
                    " PRIMARY KEY (`DiscordId`))";
            stmt.executeUpdate(sql);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e){
            e.printStackTrace();
            Utility.sleep(60000);
            System.out.println("Reconnecting to database...");
            return getConnection();
        }
    }

    public void createRecord(long discordID, long planetsideId) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO `Verification` (DiscordId, PlanetsideId) VALUES (?, ?)")) {

            ps.setLong(1, discordID);
            ps.setLong(2, planetsideId);

            ps.execute();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void deleteRecordDiscord(long discordId) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM `Verification` WHERE DiscordId = ?")) {
            ps.setLong(1, discordId);

            ps.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void deleteRecordPlanetside(long planetsideId) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM `Verification` WHERE PlanetsideId = ?")) {
            ps.setLong(1, planetsideId);

            ps.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Optional<VerifiedData> getWithPlanetsideId(long planetsideId) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `Verification` WHERE PlanetsideId = ?")) {

            ps.setLong(1, planetsideId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                long player = rs.getLong("PlanetsideId");
                long discord = rs.getLong("DiscordId");
                Timestamp verified = rs.getTimestamp("Verified");
                VerifiedData data = new VerifiedData(player, discord, verified);
                return Optional.of(data);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<VerifiedData> getWithDiscordId(long discordID) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `Verification` WHERE DiscordId = ?")) {

            ps.setLong(1, discordID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                long player = rs.getLong("PlanetsideId");
                long discord = rs.getLong("DiscordId");
                Timestamp verified = rs.getTimestamp("Verified");
                VerifiedData data = new VerifiedData(player, discord, verified);
                return Optional.of(data);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    public List<VerifiedData> getEntries(){
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `Verification`")) {

            try (ResultSet rs = ps.executeQuery()) {
                List<VerifiedData> entries = new ArrayList<>();
                while (rs.next()){
                    long player = rs.getLong("PlanetsideId");
                    long discord = rs.getLong("DiscordId");
                    Timestamp verified = rs.getTimestamp("Verified");
                    entries.add(new VerifiedData(player, discord, verified));
                }
                return entries;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }

    }

    public Map<Long, Long> getPS2DiscordMap(){
        return getEntries().stream()
                .collect(Collectors.toMap(VerifiedData::getPs2, VerifiedData::getDiscord));
    }

    public static class VerifiedData{
        public final long ps2;
        public final long discord;
        public final Timestamp verified;
        public VerifiedData(long ps2, long discord, Timestamp verified){
            this.ps2 = ps2;
            this.discord = discord;
            this.verified = verified;
        }

        public long getPs2(){
            return ps2;
        }

        public long getDiscord(){
            return discord;
        }
    }

}
