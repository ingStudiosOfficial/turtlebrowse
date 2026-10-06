package dev.evilbrowse.managers;

import java.time.OffsetDateTime;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.ActivityType;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

public class DiscordPresenceManager {
	public static final long DEFAULT_CLIENT_ID = 1527974656840044696L;
	private static DiscordPresenceManager instance;
	private IPCClient discordIpcClient;
	private volatile long activeClientId = -1;

	private DiscordPresenceManager() {
	}

	public void init() {
		init(null);
	}

	/**
	 * Connect Discord Rich Presence. The game name Discord shows comes from the
	 * application registered under this client ID, so a custom EvilBrowse app
	 * ID can be configured in Settings → Privacy. Blank/null keeps the
	 * default.
	 */
	public synchronized void init(String clientId) {
		long id = DEFAULT_CLIENT_ID;
		if (clientId != null && !clientId.isBlank()) {
			try {
				id = Long.parseLong(clientId.trim());
			} catch (NumberFormatException e) {
				System.err.println("Invalid Discord client ID, using default: " + clientId);
				id = DEFAULT_CLIENT_ID;
			}
		}
		if (discordIpcClient != null && activeClientId == id) {
			return;
		}
		disableDiscordPresence();
		activeClientId = id;
		discordIpcClient = new IPCClient(id);
		discordIpcClient.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				updateDiscordPresence();
			}

			@Override
			public void onPacketSent(IPCClient client, Packet packet) {
			}

			@Override
			public void onPacketReceived(IPCClient client, Packet packet) {
			}

			@Override
			public void onActivityJoin(IPCClient client, String secret) {
			}

			@Override
			public void onActivitySpectate(IPCClient client, String secret) {
			}

			@Override
			public void onActivityJoinRequest(IPCClient client, String secret, User user) {
			}

			@Override
			public void onClose(IPCClient client, JsonObject json) {
			}

			@Override
			public void onDisconnect(IPCClient client, Throwable t) {
			}
		});

		Thread.ofVirtual().start(() -> {
			try {
				discordIpcClient.connect();
			} catch (NoDiscordClientException e) {
				System.err.println("No Discord client found.");
			} catch (Exception e) {
				System.err.printf("Error while connecting to Discord client: %s\n", e.getMessage());
			}
		});
	}

	public static synchronized DiscordPresenceManager getInstance() {
		if (instance == null) {
			instance = new DiscordPresenceManager();
		}
		return instance;
	}

	public void updateDiscordPresence(String details) {
		if (discordIpcClient == null)
			return;

		try {
			RichPresence.Builder builder = new RichPresence.Builder();
			builder.setDetails(details)
					.setState("EvilBrowse")
					.setStartTimestamp(OffsetDateTime.now().toEpochSecond())
					.setSmallImage("discord_presence_icon")
					.setLargeImage("discord_presence_icon")
					.setActivityType(ActivityType.Playing);
			discordIpcClient.sendRichPresence(builder.build());
		} catch (Exception e) {
			System.out.println("Failed to update Discord Presence.");
		}
	}

	public void updateDiscordPresence() {
		if (discordIpcClient == null)
			return;

		updateDiscordPresence("Browsing with EvilBrowse");
	}

	public IPCClient getClient() {
		return discordIpcClient;
	}

	public void disableDiscordPresence() {
		if (discordIpcClient == null)
			return;

		try {
			discordIpcClient.close();
		} catch (Exception ignored) {
		}
		discordIpcClient = null;
		activeClientId = -1;
	}
}
