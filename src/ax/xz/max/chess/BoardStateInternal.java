package ax.xz.max.chess;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class BoardStateInternal {
	private static final Piece[] PIECES;
	private final long[] state;

	static {
		var piecesWhite = Arrays.stream(PieceType.values()).map(type -> new Piece(Player.WHITE, type));
		var piecesBlack = Arrays.stream(PieceType.values()).map(type -> new Piece(Player.BLACK, type));

		PIECES = Stream.concat(piecesWhite, piecesBlack).toArray(Piece[]::new);
	}

    public BoardStateInternal() {
		this(new long[PIECES.length]);
	}

    public BoardStateInternal(long[] state) {
		if (state.length != PIECES.length) {
			throw new IllegalArgumentException("BoardStateInternal must have the same number of pieces as PIECES");
		}
		this.state = state;
	}

	private long allBitBoard() {
		return Arrays.stream(state).reduce(0L, (a, b) -> a | b);
	}

	private static boolean bitAt(long l, int rank, int file) {
		return (l & (1L << (rank * 8 + file))) != 0;
	}

	public int numPieces() {
		return (int) Arrays.stream(state).map(Long::bitCount).sum();
	}

	public Piece get(int rank, int file) {
		for (int i = 0; i < PIECES.length; i++) {
			if (bitAt(state[i], rank, file)) {
				return PIECES[i];
			}
		}

		return null;
	}

	void set(Piece piece, int rank, int file) {
		for (int i = 0; i < PIECES.length; i++) {
			if (PIECES[i].equals(piece)) {
				state[i] |= (1L << (rank * 8 + file));
			} else {
				state[i] &= ~(1L << (rank * 8 + file));
			}
		}
	}

	public Piece[] getRank(int rank) {
		Piece[] result = new Piece[8];
		for (int i = 0; i < 8; i++) {
			result[i] = get(rank, i);
		}
		return result;
	}

	public long bitBoardFor(Piece piece) {
		int index = indexOf(piece);
		if (index == -1)
			throw new IllegalArgumentException("Piece not found in BoardStateInternal");
		return state[index];
	}

	public Iterable<Iterable<Piece>> ranksReversed() {
		return () -> new Iterator<>() {
			private int rank = 7;

			@Override
			public boolean hasNext() {
				return rank >= 0;
			}

			@Override
			public Iterable<Piece> next() {
				return rank(rank--);
			}
		};
	}

	public static int indexOf(Piece piece) {
		for (int i = 0; i < PIECES.length; i++) {
			if (PIECES[i].equals(piece))
				return i;
		}
		return -1;
	}

	public Iterable<BoardCoordinate> allOf(Piece piece) {
		int index = indexOf(piece);
		if (index == -1)
			throw new IllegalArgumentException("Piece not found in BoardStateInternal");
		return () -> new Iterator<>() {
			private long remaining = state[index];

			@Override
			public boolean hasNext() {
				return remaining != 0;
			}

			@Override
			public BoardCoordinate next() {
				int location = Long.numberOfTrailingZeros(remaining);
				remaining &= ~(1L << location);
				return new BoardCoordinate(location / 8, location % 8);
			}
		};
	}

	public Iterable<BoardCoordinate> allPieces() {
		return () -> new Iterator<>() {
			private long remaining = allBitBoard();

			@Override
			public boolean hasNext() {
				return remaining != 0;
			}

			@Override
			public BoardCoordinate next() {
				int location = Long.numberOfTrailingZeros(remaining);
				remaining &= ~(1L << location);
				return new BoardCoordinate(location / 8, location % 8);
			}
		};
	}

	public Iterable<Piece> rank(int rank) {
		return () -> new Iterator<>() {
			private int file = 0;

			@Override
			public boolean hasNext() {
				return file < 8;
			}

			@Override
			public Piece next() {
				return get(rank, file++);
			}
		};
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(state);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BoardStateInternal other = (BoardStateInternal) obj;
		return Arrays.equals(state, other.state);
	}

	public BoardStateInternal copy() {
		return new BoardStateInternal(Arrays.copyOf(state, state.length));
	}

	public BoardCoordinate findKing(Player player) {
        long bitboard = state[switch (player) {
            case WHITE -> 5;
            case BLACK -> 11;
        }];

        int index = Long.numberOfTrailingZeros(bitboard);
        if (index == 64)
            throw new IllegalStateException("King not found");

        return new BoardCoordinate(index / 8, index % 8);
    }

	@Override
	public String toString() {
		return "BoardStateInternal" + Arrays.toString(state);
	}

}
