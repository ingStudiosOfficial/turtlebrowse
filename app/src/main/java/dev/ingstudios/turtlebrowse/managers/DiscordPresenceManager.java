package dev.ingstudios.turtlebrowse.managers;

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
	private static DiscordPresenceManager instance;
	private IPCClient discordIpcClient;

	private DiscordPresenceManager() {
	}

	public void init() {
		discordIpcClient = new IPCClient(1527974656840044696L);
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
					.setStartTimestamp(OffsetDateTime.now().toEpochSecond())
					.setSmallImage("discord_presence_icon")
					.setLargeImage("discord_presence_icon")
					.setActivityType(ActivityType.Playing);
			discordIpcClient.sendRichPresence(builder.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateDiscordPresence() {
		if (discordIpcClient == null)
			return;

		updateDiscordPresence("Browsing with Turtlebrowse");
	}

	public IPCClient getClient() {
		return discordIpcClient;
	}

	public void disableDiscordPresence() {
		if (discordIpcClient == null)
			return;

		discordIpcClient.close();
	}
}
