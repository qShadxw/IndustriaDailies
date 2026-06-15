package uk.co.tmdavies.industriadailies.objects;

import java.util.ArrayList;
import java.util.UUID;

public class DayTracker {
	public static ArrayList<UUID> players;
	public static long lastReset = 0;

	public static boolean hasGot(UUID uuid) {
		reset();

		if (players == null) {
			players = new ArrayList<>();
			return false;
		}

        return players.contains(uuid);
    }

	public static void add(UUID uuid) {
		reset();

		if (players == null) {
			players = new ArrayList<>();
		}

		if (players.contains(uuid)) {
			return;
		}

		players.add(uuid);
	}

	private static void reset() {
		if (lastReset + 86400 <= System.currentTimeMillis() / 1000) {
			lastReset = System.currentTimeMillis() / 1000;
			players = new ArrayList<>();
		}
	}

	public static void forceReset() {
		lastReset = 0;
		players = new ArrayList<>();
	}
}
