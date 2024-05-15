package ax.xz.max.chess.online;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.engine.choice.FasterAlphaBetaSearch;
import ax.xz.max.chess.engine.choice.MovePicker;
import ax.xz.max.chess.engine.choice.RandomMovePicker;
import ax.xz.max.chess.engine.evaluators.PieceMapEvaluator;
import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.model.Event.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BotServer {
	private static final ClientAuth client;
	private static final UserAuth profile;

	static {
		var clientProfile = ClientProfile.initialize();
		client = clientProfile.client();
		profile = clientProfile.profile();
	}

	public static void main(String[] args) {
		while(true) {
			try {
				// Connect the BOT to Lichess
				var connectResult = client.bot().connect();

				// Check for failure
				if (!(connectResult instanceof Entries(var events))) {
					System.out.println("Failed to connect: " +connectResult);
					TimeUnit.SECONDS.sleep(60);
					continue;
				}

				// Listen for game start events and incoming challenges
				events.forEach(event -> { switch(event) {
					case ChallengeCreatedEvent created -> executor.submit(() -> handleChallenge(created));
					case GameStartEvent(var game, _) -> executor.submit(() -> {
						try {
							playGame(game);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					});
					default -> System.out.println("Misc event: " + event);
				}});

				Thread.sleep(1000);

			} catch (Exception e) {
				System.out.println("Exception: " + e);
			}
		}
	}

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	private static void handleChallenge(ChallengeCreatedEvent challengeEvent) {
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

		// todo: check ongoing games or smth

		var acceptResult = client.challenges().acceptChallenge(challenge.id());

		if (acceptResult instanceof Fail<?> f) {
			System.out.println("Failed to connect to challenge: " + f);
			return;
		}

		System.out.println("Accepted challenge from " + challenger);

		String greeting = challengeEvent.isRematch() ? "Again " + challenger.user().name() : "Good luck " + challenger.user().name();
		client.bot().chat(challenge.id(), greeting);
	}

	private static final MovePicker movePicker = new FasterAlphaBetaSearch(new PieceMapEvaluator(), 5);

	private static void playGame(GameInfo game) throws InterruptedException {
		String opponent = game.opponent().name();

		var white = game.color() == Color.white ? profile.name() : opponent;
		var black = game.color() == Color.black ? profile.name() : opponent;

		Board startBoard = Board.fromFEN(game.fen());

		AtomicBoolean gameEnded = new AtomicBoolean(false);
		if (game.isMyTurn()) {
			var move = movePicker.chooseNextMove(startBoard);
			client.bot().move(game.gameId(), move.toUCI());
		}

		while (!gameEnded.get()) {
			client.bot().connectToGame(game.gameId()).stream()
					.forEach(event -> { switch (event) {
						case GameStateEvent.Chat chat -> System.out.println("(Chat) " + chat.username() + ": " + chat.text());
						case GameStateEvent.OpponentGone _ -> System.out.println("Opponent gone");
						case GameStateEvent.Full full -> gameEnded.set(handleGameState(startBoard, game.gameId(), game, full.state()));
						case GameStateEvent.State state -> gameEnded.set(handleGameState(startBoard, game.gameId(), game, state));
					}});
			Thread.sleep(100);
		}

	}

	/** returns whether the game is finished */
	private static boolean handleGameState(Board startBoard, String gameID, GameInfo game, GameStateEvent.State state) {
		if (state.status().ordinal() > Status.started.ordinal()) {
			System.out.println("Game over");
			return true;
		}

		var moveList = state.moveList();
		Board board = startBoard.copy();
		for (String move : moveList) {
			board.makeMove(board.fromUCI(move));
		}

		var toMove = switch (board.currentTurn()) {
			case WHITE -> Color.white; // cross-library enums
			case BLACK -> Color.black;
		};

		if (state.drawOffer() instanceof Some(var drawOffer)) {
			System.out.println("Draw offer from " + drawOffer);
			client.bot().chat(game.gameId(), "I accept draws");
			client.bot().handleDrawOffer(game.gameId(), true); // accept draws
		}

		if (toMove == game.color()) {
			var move = movePicker.chooseNextMove(board);
			client.bot().move(game.gameId(), move.toUCI());
		}

		return false;
	}
}
