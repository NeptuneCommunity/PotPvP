package net.frozenorb.potpvp;

import com.comphenix.protocol.ProtocolLibrary;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Setter;
import net.frozenorb.chunksnapshot.ChunkSnapshot;
import net.frozenorb.potpvp.arena.ArenaHandler;
import net.frozenorb.potpvp.duel.DuelHandler;
import net.frozenorb.potpvp.elo.EloHandler;
import net.frozenorb.potpvp.follow.FollowHandler;
import net.frozenorb.potpvp.kit.KitHandler;
import net.frozenorb.potpvp.kittype.KitType;
import net.frozenorb.potpvp.kittype.KitTypeJsonAdapter;
import net.frozenorb.potpvp.listener.*;
import net.frozenorb.potpvp.lobby.LobbyHandler;
import net.frozenorb.potpvp.match.Match;
import net.frozenorb.potpvp.match.MatchHandler;
import net.frozenorb.potpvp.nametag.framework.NametagHandler;
import net.frozenorb.potpvp.party.PartyHandler;
import net.frozenorb.potpvp.postmatchinv.PostMatchInvHandler;
import net.frozenorb.potpvp.queue.QueueHandler;
import net.frozenorb.potpvp.rematch.RematchHandler;
import net.frozenorb.potpvp.scoreboard.PotPvPScoreboardConfiguration;
import net.frozenorb.potpvp.scoreboard.assemble.AssembleStyle;
import net.frozenorb.potpvp.scoreboard.assemble.ScoreboardHandler;
import net.frozenorb.potpvp.setting.SettingHandler;
import net.frozenorb.potpvp.statistics.StatisticsHandler;
import net.frozenorb.potpvp.tab.tab.TabAdapter;
import net.frozenorb.potpvp.tab.tab.TabHandler;
import net.frozenorb.potpvp.tournament.TournamentHandler;
import net.frozenorb.potpvp.util.ItemUtil;
import net.frozenorb.potpvp.util.command.CommandHandler;
import net.frozenorb.potpvp.util.menu.framework.ButtonListener;
import net.frozenorb.potpvp.util.protocol.InventoryAdapter;
import net.frozenorb.potpvp.util.protocol.LagCheck;
import net.frozenorb.potpvp.util.protocol.PingAdapter;
import net.frozenorb.potpvp.util.serialization.*;
import net.frozenorb.potpvp.util.uuid.UUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.craftbukkit.libs.com.google.gson.TypeAdapter;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonReader;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.IOException;

public final class PotPvPSI extends JavaPlugin {

    @Getter
    private static PotPvPSI instance;

    @Getter
    @Setter
    private static Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
            .registerTypeHierarchyAdapter(Vector.class, new VectorAdapter())
            .registerTypeAdapter(BlockVector.class, new BlockVectorAdapter())
            .registerTypeHierarchyAdapter(KitType.class, new KitTypeJsonAdapter()) // custom KitType serializer
            .registerTypeAdapter(ChunkSnapshot.class, new ChunkSnapshotAdapter())
            .serializeNulls()
            .create();

    public static Gson plainGson = new GsonBuilder()
            .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
            .registerTypeHierarchyAdapter(Vector.class, new VectorAdapter())
            .registerTypeAdapter(BlockVector.class, new BlockVectorAdapter())
            .serializeNulls()
            .create();

    @Getter
    private MongoClient mongoClient;
    @Getter
    private MongoDatabase mongoDatabase;

    @Getter
    private SettingHandler settingHandler;
    @Getter
    private DuelHandler duelHandler;
    @Getter
    private KitHandler kitHandler;
    @Getter
    private LobbyHandler lobbyHandler;
    @Getter
    private ArenaHandler arenaHandler;
    @Getter
    private MatchHandler matchHandler;
    @Getter
    private PartyHandler partyHandler;
    @Getter
    private QueueHandler queueHandler;
    @Getter
    private RematchHandler rematchHandler;
    @Getter
    private PostMatchInvHandler postMatchInvHandler;
    @Getter
    private FollowHandler followHandler;
    @Getter
    private EloHandler eloHandler;
    @Getter
    private TournamentHandler tournamentHandler;
    @Getter
    public UUIDCache uuidCache;

    @Getter
    public ScoreboardHandler scoreboardHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        setupMongo();

        for (World world : Bukkit.getWorlds()) {
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setTime(6_000L);
        }

        /* Initializing */
        uuidCache = new UUIDCache();

        CommandHandler.init();
        CommandHandler.registerAll(this);

        TabHandler.init();

        PotPvPScoreboardConfiguration scoreboardAdapter = new PotPvPScoreboardConfiguration();
        scoreboardHandler = new ScoreboardHandler(this, scoreboardAdapter);
        scoreboardHandler.setAssembleStyle(AssembleStyle.MODERN);
        scoreboardHandler.setTicks(10L);

        NametagHandler.init();

        ItemUtil.load();

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PingAdapter());
            new LagCheck().runTaskTimerAsynchronously(PotPvPSI.this, 100L, 100L);
            ProtocolLibrary.getProtocolManager().addPacketListener(new TabAdapter());
            ProtocolLibrary.getProtocolManager().addPacketListener(new InventoryAdapter());
        }

        settingHandler = new SettingHandler();
        duelHandler = new DuelHandler();
        kitHandler = new KitHandler();
        lobbyHandler = new LobbyHandler();
        arenaHandler = new ArenaHandler();
        matchHandler = new MatchHandler();
        partyHandler = new PartyHandler();
        queueHandler = new QueueHandler();
        rematchHandler = new RematchHandler();
        postMatchInvHandler = new PostMatchInvHandler();
        followHandler = new FollowHandler();
        eloHandler = new EloHandler();
        tournamentHandler = new TournamentHandler();

        getServer().getPluginManager().registerEvents(new BasicPreventionListener(), this);
        getServer().getPluginManager().registerEvents(new BowHealthListener(), this);
        getServer().getPluginManager().registerEvents(new ChatFormatListener(), this);
        getServer().getPluginManager().registerEvents(new ChatToggleListener(), this);
        getServer().getPluginManager().registerEvents(new NightModeListener(), this);
        getServer().getPluginManager().registerEvents(new PearlCooldownListener(), this);
//        getServer().getPluginManager().registerEvents(new RankedMatchQualificationListener(), this);
        getServer().getPluginManager().registerEvents(new TabCompleteListener(), this);
        getServer().getPluginManager().registerEvents(new StatisticsHandler(), this);

        getServer().getPluginManager().registerEvents(new ButtonListener(), this);
    }

    @Override
    public void onDisable() {
        for (Match match : this.matchHandler.getHostedMatches()) {
            if (match.getKitType().isBuildingAllowed()) match.getArena().restore();
        }

        try {
            arenaHandler.saveSchematics();
        } catch (IOException e) {
            e.printStackTrace();
        }

        instance = null;
    }


    private void setupMongo() {
        if (this.getConfig().getBoolean("MONGO.URI-MODE")) {
            this.mongoClient = MongoClients.create(this.getConfig().getString("MONGO.URI.CONNECTION_STRING"));
            this.mongoDatabase = mongoClient.getDatabase(this.getConfig().getString("MONGO.URI.DATABASE"));

            PotPvPSI.getInstance().getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Practice] " + ChatColor.BLUE + "Successfully loaded mongo.");
            return;
        }

        boolean auth = this.getConfig().getBoolean("MONGO.NORMAL.AUTHENTICATION.ENABLED");
        String host = this.getConfig().getString("MONGO.NORMAL.HOST");
        int port = this.getConfig().getInt("MONGO.NORMAL.PORT");

        String uri = "mongodb://" + host + ":" + port;

        if (auth) {
            String username = this.getConfig().getString("MONGO.NORMAL.AUTHENTICATION.USERNAME");
            String password = this.getConfig().getString("MONGO.NORMAL.AUTHENTICATION.PASSWORD");
            uri = "mongodb://" + username + ":" + password + "@" + host + ":" + port;
        }

        this.mongoClient = MongoClients.create(uri);
        this.mongoDatabase = mongoClient.getDatabase(this.getConfig().getString("MONGO.URI.DATABASE"));
        PotPvPSI.getInstance().getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Practice] " + ChatColor.BLUE + "Successfully loaded mongo.");
    }

    // This is here because chunk snapshots are (still) being deserialized, and serialized sometimes.
    private static class ChunkSnapshotAdapter extends TypeAdapter<ChunkSnapshot> {

        @Override
        public ChunkSnapshot read(JsonReader arg0) throws IOException {
            return null;
        }

        @Override
        public void write(JsonWriter arg0, ChunkSnapshot arg1) throws IOException {

        }
    }

}