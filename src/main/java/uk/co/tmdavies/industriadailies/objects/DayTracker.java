package uk.co.tmdavies.industriadailies.objects;


import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class DayTracker {
	public static ArrayList<UUID> players;
	public static long lastReset = 0;

	public static boolean hasGot(UUID uuid)
	{
		reset();

		if (players == null)
		{
			players = new ArrayList<>();
			return false;
		}

		if (players.contains(uuid))
		{
			return true;
		}

		return false;
	}

	public static void add(UUID uuid)
	{
		reset();

		if (players == null)
		{
			players = new ArrayList<>();
		}

		if (players.contains(uuid))
		{
			return;
		}

		players.add(uuid);
	}

	private static void reset()
	{
		//ZonedDateTime nowZoned = ZonedDateTime.now();
		//Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
		//Duration duration = Duration.between(midnight, Instant.now());
		//long seconds = duration.getSeconds();


		if (lastReset + 86400 <= System.currentTimeMillis() / 1000)
		{
			lastReset = System.currentTimeMillis() / 1000;
			players = new ArrayList<>();
		}
	}

	public static void forceReset()
	{
		lastReset = 0;
		players = new ArrayList<>();
	}





}
