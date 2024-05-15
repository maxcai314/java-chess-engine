package ax.xz.max.chess.online;

import chariot.*;
import chariot.Client.*;
import chariot.model.*;

import java.net.URI;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Code from tors42 for connecting to Lichess API
 */
record ClientProfile(ClientAuth client, UserAuth profile) {
	
	static ClientProfile initialize() {
		var client  = initializeClient();
		var profile = initializeProfile(client);

		return new ClientProfile(client, profile);
	}

	/**
	 * Initialize client with a OAuth token with scope bot:play,
	 * either provided via environment variable BOT_TOKEN (create at https://lichess.org/account/oauth/token/create),
	 * or using OAuth PKCE flow and storing granted token locally with Java Preferences API,
	 * i.e. at first run the user must interactively grant access by navigating with a Web Browser
	 * to the Lichess grant page and authorizing with the Bot Account,
	 * and consecutive runs the now already stored token will be used automatically.
	 */
	static ClientAuth initializeClient() {
		var prefs = Preferences.userRoot().node(System.getProperty("prefs", "charibot"));

		URI lichessApi = URI.create(System.getenv("LICHESS_API") instanceof String api ? api : "https://lichess.org");

		if (System.getenv("BOT_TOKEN") instanceof String token) { // scope bot:play
			var client = Client.auth(conf -> conf.api(lichessApi), token);
			if (client.scopes().contains(Scope.bot_play)) {
				System.out.println("Storing and using provided token " + prefs);
				client.store(prefs);
				return client;
			}
			System.out.println("Provided token is missing bot:play scope");
			throw new RuntimeException("BOT_TOKEN is missing scope bot:play");
		}

		var client = Client.load(prefs);

		if (client instanceof ClientAuth auth
				&& auth.scopes().contains(Scope.bot_play)) {
			System.out.println("Using stored token " + prefs);
			return auth;
		}

		var authResult = Client.auth(
				conf -> conf.api(lichessApi),
				uri -> System.out.println(
						"Visit the following URL and choose to grant access to this application or not: " + uri
				),
				pkce -> pkce.scope(Scope.bot_play));

		if (!(authResult instanceof AuthOk(var auth))) {
			throw new RuntimeException("OAuth PKCE flow failed: " + authResult);
		}

		System.out.println("OAuth PKCE flow succeeded - storing and using token " + prefs);
		auth.store(prefs);

		return auth;
	}

	static UserAuth initializeProfile(ClientAuth client) {
		// Check the Lichess account
		var profileResult = client.account().profile();
		if (!(profileResult instanceof Entry(var profile))) {
			throw new RuntimeException("Failed to lookup bot account profile: " + profileResult);
		}

		// Lichess account is a BOT account, we're done - return profile
		if (profile.title() instanceof Some(var title) && "BOT".equals(title)) return profile;

		// It wasn't a BOT account...
		System.out.println(profile.name() + " is not a bot account");

		// Check if eligible to upgrade to BOT account
		if (profile.accountStats().all() > 0) {
			System.out.println("Account has played games - won't be possible to upgrade to BOT account");
			throw new RuntimeException("Not a bot account (and not upgradeable because there are played games)");
		}

		// Upgrade to BOT account - attempt to do so
		if (client.bot().upgradeToBotAccount() instanceof Fail<?> fail) {
			throw new RuntimeException("Failed to upgrade account to bot account: " + fail);
		}

		// Tada!
		System.out.println("Upgraded account to bot account");
		return profile;
	}
}