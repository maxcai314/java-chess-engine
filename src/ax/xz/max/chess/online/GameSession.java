package ax.xz.max.chess.online;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.Player;
import ax.xz.max.chess.engine.choice.MovePicker;
import ax.xz.max.chess.moves.PlayerMove;
import chariot.api.BotAuth;
import chariot.model.*;

class GameSession {
	private final String gameId;
	private final BotAuth bot;
	private final Board startingBoard;
	private final Player player;
	private final MovePicker movePicker;
	private volatile int previousMoves; // the amount of moves already present before starting the game


	 GameSession(String gameId, BotAuth bot, Board startingBoard, Player player, MovePicker movePicker) {
		this.gameId = gameId;
		this.bot = bot;
		this.startingBoard = startingBoard;
		this.player = player;
		this.movePicker = movePicker;
	}

	private static Player parseColor(Enums.Color color) {
		return switch (color) {
			case white -> Player.WHITE;
			case black -> Player.BLACK;
		};
	}

	static GameSession fromGameInfo(GameInfo gameInfo, BotAuth botAuth, MovePicker movePicker) {
		var gameId = gameInfo.gameId();
		var startingBoard = Board.fromFEN(gameInfo.fen());
		var player = parseColor(gameInfo.color());

		return new GameSession(gameId, botAuth, startingBoard, player, movePicker);
	}

	void playGame() {
		bot.connectToGame(gameId).stream().forEach(this::handleGameEvent);
	}

	private void handleGameEvent(GameStateEvent event) {
		System.out.println("Game event: " + event);
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
		try {
			System.out.println("Game state: " + state);
			if (state.status().ordinal() > Enums.Status.started.ordinal()) {
				System.out.println("Game over");
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
			if (movesFirst == evenMoves) return; // not our turn

			Board board = startingBoard.copy();
			for (String move : moveList)
				board.makeMove(board.fromUCI(move));

			var move = movePicker.chooseNextMove(board);
			sendMove(move);
		} catch (Exception e) {
			System.err.println("Error in game " + gameId);
		}
	}

	public void chat(String message) {
		bot.chat(gameId, message);
	}

	private void sendMove(PlayerMove move) {
		bot.move(gameId, move.toUCI());
	}
}
