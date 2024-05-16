package ax.xz.max.chess.online;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.Player;
import ax.xz.max.chess.engine.choice.FasterAlphaBetaSearch;
import ax.xz.max.chess.engine.choice.MovePicker;
import ax.xz.max.chess.engine.evaluators.BoardEvaluator;
import ax.xz.max.chess.engine.evaluators.PieceMapEvaluator;
import ax.xz.max.chess.moves.PlayerMove;
import chariot.api.BotAuth;
import chariot.model.*;

import java.time.Duration;
import java.time.Instant;

class GameSession {
	private final String gameId;
	private final BotAuth bot;
	private final Board startingBoard;
	private final Player player;
	private volatile int previousMoves; // the amount of moves already present before starting the game


	 GameSession(String gameId, BotAuth bot, Board startingBoard, Player player) {
		this.gameId = gameId;
		this.bot = bot;
		this.startingBoard = startingBoard;
		this.player = player;
	}

	private static Player parseColor(Enums.Color color) {
		return switch (color) {
			case white -> Player.WHITE;
			case black -> Player.BLACK;
		};
	}

	static GameSession fromGameInfo(GameInfo gameInfo, BotAuth botAuth) {
		var gameId = gameInfo.gameId();
		var startingBoard = Board.fromFEN(gameInfo.fen());
		var player = parseColor(gameInfo.color());

		return new GameSession(gameId, botAuth, startingBoard, player);
	}

	void playGame() {
		bot.connectToGame(gameId).stream().forEach(this::handleGameEvent);
	}

	private void handleGameEvent(GameStateEvent event) {
		switch (event) {
			case GameStateEvent.Chat chat -> System.out.println("(Chat) " + chat.username() + ": " + chat.text());
			case GameStateEvent.OpponentGone _ -> System.out.println("Opponent gone");
			case GameStateEvent.Full full -> handleFullState(full);
			case GameStateEvent.State state -> handleGameState(state);
		}
	}

	private void handleFullState(GameStateEvent.Full full) {
		 var state = full.state();
		 previousMoves = state.moveList().size();
		 handleGameState(state);
	}

	private void handleGameState(GameStateEvent.State state) {
		Thread.ofVirtual().start(() -> handleGameState0(state));
	}

	private void handleGameState0(GameStateEvent.State state) {
		try {
			if (state.status().ordinal() > Enums.Status.started.ordinal()) {
				System.out.println("Game over");
				chat("good game!");
				return;
			}

			if (state.drawOffer().map(GameSession::parseColor) instanceof Some(Player drawOffer)) {
				System.out.println("Draw offer from " + drawOffer);
				if (drawOffer == player.opponent()) {
					chat("I accept draws");
					bot.handleDrawOffer(gameId, true); // accept draws
				}
			}

			var moveList = state.moveList().subList(previousMoves, state.moveList().size());

			boolean movesFirst = player == startingBoard.currentTurn();
			boolean evenMoves = moveList.size() % 2 == 0;
			if (movesFirst != evenMoves) return; // not our turn

			Board board = startingBoard.copy();
			for (String move : moveList)
				board.makeMove(board.fromUCI(move));

			Duration timeRemaining = switch (player) {
				case WHITE -> state.wtime();
				case BLACK -> state.btime();
			};

			BoardEvaluator heuristic = new PieceMapEvaluator();
			int searchDepth = searchDepthFor(timeRemaining);
			System.out.println("Using search depth " + searchDepth);
			MovePicker engine = new FasterAlphaBetaSearch(heuristic, searchDepth);

			Instant start = Instant.now();
			var move = engine.chooseNextMove(board);
			System.out.println("Move picked in " + Duration.between(start, Instant.now()).toMillis() + "ms: " + move.toUCI());
			sendMove(move);
		} catch (Exception e) {
			System.err.println("Error in game " + gameId);
		}
	}

	private int searchDepthFor(Duration time) {
		double minutesLeft = time.getSeconds() / 60.;
		if (minutesLeft < 0.1) return 3;
		if (minutesLeft < 1) return 4;
		if (minutesLeft < 5) return 5;
		if (minutesLeft < 20) return 6;
		else return 7;
	}

	public void chat(String message) {
		bot.chat(gameId, message);
	}

	private void sendMove(PlayerMove move) {
		bot.move(gameId, move.toUCI());
	}
}
