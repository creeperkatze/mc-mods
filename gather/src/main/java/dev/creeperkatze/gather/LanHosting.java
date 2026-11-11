package dev.creeperkatze.gather;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.creeperkatze.gather.mixin.IntegratedServerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.server.LanServerPinger;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.StoredUserList;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class LanHosting {
	public static final SimpleCommandExceptionType BAN_OWNER = new SimpleCommandExceptionType(Component.translatable("gather.commands.ban.owner"));
	private static final List<String> VIRTUAL_ADAPTERS = List.of("virtualbox", "vmware", "vbox", "hyper-v", "vethernet", "docker", "wsl");

	private LanHosting() {
	}

	private static Path worldDir(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT);
	}

	public static HostSettings settings(IntegratedServer server) {
		return ((HostedServer) server).gather$settings();
	}

	// Runs once the world is loaded, before anyone can join
	public static void start(IntegratedServer server) {
		HostSettings settings = HostSettings.load(worldDir(server));
		((HostedServer) server).gather$setSettings(settings);
		loadPlayerLists(server);
		server.setEnforceWhitelist(true);
		applyToServer(server, settings);
	}

	// Vanilla never loads these lists for a world it hosts
	private static void loadPlayerLists(IntegratedServer server) {
		PlayerList players = server.getPlayerList();
		for (StoredUserList<?, ?> list : List.of(players.getBans(), players.getIpBans(), players.getWhiteList())) {
			try {
				list.load();
			} catch (IOException e) {
				Gather.LOGGER.warn("Couldn't load {}", list.getFile(), e);
			}
		}
	}

	// Bans and the whitelist belong to the world, not to the whole game
	public static File playerListFile(MinecraftServer server, File file) {
		return server instanceof IntegratedServer ? worldDir(server).resolve(file.getName()).toFile() : file;
	}

	public static void update(IntegratedServer server, HostSettings settings) {
		String previousMotd = server.getMotd();
		((HostedServer) server).gather$setSettings(settings);
		applyToServer(server, settings);
		settings.save(worldDir(server));
		server.kickUnlistedPlayers();
		// The LAN broadcast keeps the name it was started with
		if (server.isPublished() && !previousMotd.equals(server.getMotd())) {
			restartBroadcast(server);
		}
	}

	private static void applyToServer(IntegratedServer server, HostSettings settings) {
		server.setUsesAuthentication(settings.onlineMode());
		server.setMotd(settings.serverName().isBlank() ? defaultServerName(server) : ServerNameFormat.toLegacy(settings.serverName()));
		server.setUsingWhitelist(settings.whitelist());
		server.setPlayerIdleTimeout(settings.idleTimeout());
	}

	// The whitelist and idle timeout can also change through commands
	public static void onWhitelistChanged(MinecraftServer server, boolean whitelist) {
		if (server instanceof IntegratedServer integrated && settings(integrated).whitelist() != whitelist) {
			save(integrated, settings(integrated).withWhitelist(whitelist));
		}
	}

	public static void onIdleTimeoutChanged(MinecraftServer server, int idleTimeout) {
		if (server instanceof IntegratedServer integrated && settings(integrated).idleTimeout() != idleTimeout) {
			save(integrated, settings(integrated).withIdleTimeout(idleTimeout));
		}
	}

	private static void save(IntegratedServer server, HostSettings settings) {
		((HostedServer) server).gather$setSettings(settings);
		settings.save(worldDir(server));
	}

	// Bans, the whitelist, the player limit and idle kicks never apply to the host
	public static boolean isHost(MinecraftServer server, NameAndId player) {
		return server instanceof IntegratedServer && server.isSingleplayerOwner(player);
	}

	public static boolean isHost(MinecraftServer server, GameProfile profile) {
		return isHost(server, new NameAndId(profile));
	}

	// The host can always manage who joins, even when the world doesn't allow cheats
	public static Predicate<CommandSourceStack> orHost(Predicate<CommandSourceStack> requirement) {
		return source -> requirement.test(source)
				|| source.getEntity() instanceof ServerPlayer player && isHost(source.getServer(), player.getGameProfile());
	}

	// The name vanilla gives a LAN world
	public static String defaultServerName(MinecraftServer server) {
		GameProfile host = server.getSingleplayerProfile();
		String levelName = server.getWorldData().getLevelName();
		return host != null ? host.name() + " - " + levelName : levelName;
	}

	private static void restartBroadcast(IntegratedServer server) {
		IntegratedServerAccessor accessor = (IntegratedServerAccessor) server;
		LanServerPinger previous = accessor.gather$getLanPinger();
		if (previous != null) {
			previous.interrupt();
		}
		try {
			LanServerPinger pinger = new LanServerPinger(server.getMotd(), Integer.toString(server.getPort()));
			pinger.start();
			accessor.gather$setLanPinger(pinger);
		} catch (IOException e) {
			Gather.LOGGER.warn("Couldn't restart the LAN broadcast", e);
			accessor.gather$setLanPinger(null);
		}
	}

	// Remembers how the world was opened, so it can open the same way next time
	public static void rememberPublished(IntegratedServer server) {
		if (server.isPublished()) {
			//? if >=26.3 {
			save(server, settings(server).withPublished(server.getGuestCommandAccess(), server.forceGameMode()));
			//?} else if >=26.2 {
			/*save(server, settings(server).withPublished(server.commandsAllowedForOtherPlayers(), server.getGameTypeForOtherPlayers()));
			*///?} else {
			/*save(server, settings(server).withPublished(server.getPlayerList().isAllowCommandsForAllPlayers(), server.getForcedGameType()));
			*///?}
		}
	}

	// Vanilla only reloads the whitelist file on dedicated servers
	public static void reloadWhitelist(MinecraftServer server) {
		if (server instanceof IntegratedServer) {
			try {
				server.getPlayerList().getWhiteList().load();
			} catch (IOException e) {
				Gather.LOGGER.warn("Couldn't reload the whitelist", e);
			}
		}
	}

	// Replaces the vanilla message, which only shows the port
	public static Optional<MutableComponent> publishedMessage(int port) {
		return lanAddress().map(address -> Component.translatable("gather.published", ComponentUtils.copyOnClickText(address + ":" + port)));
	}

	public static void openOnLoad(Minecraft minecraft) {
		IntegratedServer server = minecraft.getSingleplayerServer();
		if (server == null || server.isPublished() || !settings(server).openOnLoad()) {
			return;
		}
		HostSettings settings = settings(server);
		int port = HttpUtil.getAvailablePort();
		//? if >=26.3 {
		server.setGuestCommandAccess(settings.guestCommands());
		server.setForceGameMode(settings.forceGameMode());
		boolean published = server.publishServer(MinecraftServer.MultiplayerScope.LAN, port);
		//?} else if >=26.2 {
		/*boolean published = server.publishServer(MinecraftServer.MultiplayerScope.LAN, settings.guestGameMode(), settings.guestCommands(), port);
		*///?} else {
		/*boolean published = server.publishServer(settings.guestGameMode(), settings.guestCommands(), port);
		*///?}
		chat(published ? PublishCommand.getSuccessMessage(port) : Component.translatable("commands.publish.failed"));
	}

	private static void chat(Component message) {
		//? if >=26.2 {
		Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(message);
		//?} else {
		/*Minecraft.getInstance().gui.getChat().addMessage(message);
		*///?}
	}

	// Players with a real account keep their real id in offline mode, so their inventory stays
	public static GameProfile offlineProfile(MinecraftServer server, String name, Function<String, GameProfile> offline) {
		if (server instanceof IntegratedServer) {
			Optional<NameAndId> known = server.services().nameToIdCache().get(name);
			if (known.isPresent()) {
				return new GameProfile(known.get().id(), name);
			}
		}
		return offline.apply(name);
	}

	// Vanilla lets anyone who uses the host's name in as the host
	public static boolean takesHostName(MinecraftServer server, Connection connection, String name) {
		GameProfile host = server.getSingleplayerProfile();
		return server instanceof IntegratedServer && host != null && !connection.isMemoryConnection() && host.name().equalsIgnoreCase(name);
	}

	private static Optional<String> lanAddress() {
		try {
			for (NetworkInterface network : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (!network.isUp() || network.isLoopback() || network.isVirtual() || isVirtualAdapter(network)) {
					continue;
				}
				for (InetAddress address : Collections.list(network.getInetAddresses())) {
					if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
						return Optional.of(address.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			Gather.LOGGER.warn("Couldn't find the LAN address", e);
		}
		return Optional.empty();
	}

	// Adapters of virtual machines and containers have addresses friends can't reach, VPNs like Hamachi are kept
	private static boolean isVirtualAdapter(NetworkInterface network) {
		String name = (network.getName() + " " + network.getDisplayName()).toLowerCase(Locale.ROOT);
		return VIRTUAL_ADAPTERS.stream().anyMatch(name::contains);
	}
}
