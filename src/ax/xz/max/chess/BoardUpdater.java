package ax.xz.max.chess;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface BoardUpdater extends UnaryOperator<BoardState> {
	@Override
	BoardState apply(BoardState state);
}