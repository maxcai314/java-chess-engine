package ax.xz.max.chess.online;

import ax.xz.max.chess.engine.choice.FasterAlphaBetaSearch;
import ax.xz.max.chess.engine.choice.MovePicker;
import ax.xz.max.chess.engine.evaluators.BoardEvaluator;
import ax.xz.max.chess.engine.evaluators.PieceMapEvaluator;
import chariot.ClientAuth;
import chariot.model.*;

public class LichessBotServer {
	private final ClientAuth client;
	private final UserAuth profile;
	public LichessBotServer(ClientProfile clientProfile) {
		client = clientProfile.client();
		profile = clientProfile.profile();

		runServer();
	}

	private void runServer() {
		client.bot().connect().stream().forEach(this::handleEvent);
	}

	private void handleEvent(Event event) {
		switch (event) {
			case Event.ChallengeCreatedEvent created -> handleChallenge(created);
			case Event.GameStartEvent(var info, _) -> playGame(info);
			default -> System.out.println("Misc event: " + event);
		}
	}

	private void handleChallenge(Event.ChallengeCreatedEvent challengeEvent) {
		ChallengeInfo challenge = challengeEvent.challenge();

		var challenger = challenge.players().challengerOpt().orElseThrow();
		var challengeId = challenge.id();

		System.out.println("Received challenge from " + challenger + " with id " + challengeId);

		if (challenge.gameType().rated()) {
			System.out.println("Declining rated game: " + challenge);
			client.challenges().declineChallenge(challenge.id(), Enums.DeclineReason.Provider::casual);
			return;
		}

		if (challenge.gameType().variant() != VariantType.Variant.standard) {
			System.out.println("Declining non-standard variant: " + challenge);
			client.challenges().declineChallenge(challenge.id(), Enums.DeclineReason.Provider::standard);
			return;
		}

		var acceptResult = client.challenges().acceptChallenge(challenge.id());

		if (acceptResult instanceof Fail<?> f) {
			System.out.println("Failed to connect to challenge: " + f);
			return;
		}

		System.out.println("Accepted challenge from " + challenger);
	}

	private void playGame(GameInfo game) {
		System.out.println("Playing game " + game);
		BoardEvaluator heuristic = new PieceMapEvaluator();
		int searchDepth = switch (game.time().speed()) {
			case ultraBullet, bullet -> 4;
			case blitz -> 5;
			case rapid -> 6;
			case classical -> 7;
			case correspondence -> 8;
		};
		System.out.println("Using search depth " + searchDepth);

		MovePicker engine = new FasterAlphaBetaSearch(heuristic, searchDepth);

		GameSession gameSession = GameSession.fromGameInfo(game, client.bot(), engine);
		gameSession.chat("good luck");
		gameSession.playGame();
	}
}
