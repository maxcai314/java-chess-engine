package ax.xz.max.chess;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public record BoardStateInternal(long[] state) {
    private static final Piece[] PIECES;
    static {
        var piecesWhite = Arrays.stream(PieceType.values()).map(type -> new Piece(Player.WHITE, type));
        var piecesBlack = Arrays.stream(PieceType.values()).map(type -> new Piece(Player.BLACK, type));

        PIECES = Stream.concat(piecesWhite, piecesBlack).toArray(Piece[]::new);
    }

    public BoardStateInternal() {
        this(new long[PIECES.length]);
    }

    public BoardStateInternal {
        if (state.length != PIECES.length) {
            throw new IllegalArgumentException("BoardStateInternal must have the same number of pieces as PIECES");
        }
    }

    private boolean bitAt(long l, int rank, int file) {
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

    public void set(Piece piece, int rank, int file) {
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

    public int indexOf(Piece piece) {
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
        long pieces = Arrays.stream(state).reduce(0L, (a, b) -> a | b);
        return () -> new Iterator<>() {
            private long remaining = pieces;

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
}
