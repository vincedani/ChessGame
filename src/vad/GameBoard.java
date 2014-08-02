package vad;

import java.util.ArrayList;

import com.nwgjb.commons.util.BitField;

/**
 * Game board data structure. Provide a lot of helper functions. Do not transfer
 * this in networking because in Java, object streams will cache transferred
 * object. Since we only have one game board instance, do not pass this as an
 * argument, but you can use this class freely if you are sure that this class
 * will only be used locally.
 * 
 * @author Gary Guo, Vadim Korolik
 *
 */
public class GameBoard
{
	Piece[][] board;
	int currentColor = Piece.WHITE;
	byte whiteFlags;
	byte blackFlags;

	public static final int KING_MOVED_FLAG = 0;
	public static final int L_ROOK_FLAG = 1;
	public static final int R_ROOK_FLAG = 2;
	//TODO three variables can be combined into two

	public static final int[] STARTING_ROW =
	{ Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK };

	public GameBoard()
	{
		board = new Piece[8][8];
		for (int i = 0; i < 8; i++)
		{
			board[i][0] = Piece.get(Piece.BLACK, STARTING_ROW[i]);
			board[i][7] = Piece.get(Piece.WHITE, STARTING_ROW[i]);
			board[i][1] = Piece.get(Piece.BLACK, Piece.PAWN);
			board[i][6] = Piece.get(Piece.WHITE, Piece.PAWN);
		}
	}

	public GameBoard(boolean nothing)
	{
		board = new Piece[8][8];
	}

	public void setPiece(Position pos, Piece piece)
	{
		board[pos.col][pos.row] = piece;
	}

	public boolean isEmpty(Position pos)
	{
		return board[pos.col][pos.row] == null;
	}

	public Piece getPiece(Position dest)
	{
		return board[dest.col][dest.row];
	}

	public void apply(Move m)
	{
		Position start = m.getStartPosition();
		Position dest = m.getDestPosition();
		Piece startPiece = getPiece(start);

		if (m.isFirstKingMove())
			setHasKingMoved(currentColor, true);
		if (m.isFirstLRookMove())
			setHasLRookMoved(currentColor, true);
		// System.out.println("LROOK MOVE");
		if (m.isFirstRRookMove())
			setHasRRookMoved(currentColor, true);

		currentColor = Piece.getOppositeColor(currentColor);

		/* Check if it's castling */
		if (startPiece.getType() == Piece.KING)
		{
			if (start.getColumn() - 3 == dest.getColumn())
			{
				// L Castle
				Position rookPosition = Position.get(0, dest.getRow());
				setPiece(dest.getRight(), getPiece(rookPosition));
				setPiece(rookPosition, null);
			} else if (start.getColumn() + 2 == dest.getColumn())
			{
				// R Castle
				Position rookPosition = Position.get(7, dest.getRow());
				setPiece(dest.getLeft(), getPiece(rookPosition));
				setPiece(rookPosition, null);
			}
		}

		setPiece(start, null);
		setPiece(dest, startPiece);

		// System.out.println("Move apply: " + m.getKilledPiece());

	}

	public void undo(Move move)
	{
		Position start = move.getStartPosition();
		Position dest = move.getDestPosition();
		Piece movedPiece = getPiece(dest);
		if (move.isFirstKingMove())
		{
			setHasKingMoved(movedPiece.getColor(), false);
		}
		if (move.isFirstLRookMove())
		{
			setHasLRookMoved(movedPiece.getColor(), false);
			// System.out.println("LROOK UNDO");
		}
		if (move.isFirstRRookMove())
		{
			setHasRRookMoved(movedPiece.getColor(), false);
		}
		// System.out.println("Move undo: " + move.getKilledPiece());
		currentColor = Piece.getOppositeColor(currentColor);

		if (movedPiece.getType() == Piece.KING)
		{
			if (start.getColumn() - 3 == dest.getColumn())
			{
				// L Castle
				Position rookOriginalPosition = Position.get(0, dest.getRow());
				Position rookCurrentPosition = dest.getRight();
				setPiece(rookOriginalPosition, getPiece(rookCurrentPosition));
				setPiece(rookCurrentPosition, null);
			} else if (start.getColumn() + 2 == dest.getColumn())
			{
				// R Castle
				Position rookOriginalPosition = Position.get(7, dest.getRow());
				Position rookCurrentPosition = dest.getLeft();
				setPiece(rookOriginalPosition, getPiece(rookCurrentPosition));
				setPiece(rookCurrentPosition, null);
			}
		}

		setPiece(move.getStartPosition(), movedPiece);
		setPiece(move.getDestPosition(), move.getKilledPiece());
	}

	public ArrayList<Move> getAllPossibleMovesWithoutValidation(int color)
	{
		ArrayList<Move> moves = new ArrayList<>();
		for (int r = 0; r < 8; r++)
		{
			for (int c = 0; c < 8; c++)
			{
				if (isEmpty(Position.get(c, r)))
					continue;
				if (getPiece(Position.get(c, r)).getColor() == color)
				{
					moves.addAll(MoveHelper.getAllMoves4PieceWithoutValidation(this, Position.get(c, r)));
				}
			}
		}
		return moves;
	}

	public ArrayList<Move> getAllPossibleMoves(int color)
	{
		ArrayList<Move> moves = new ArrayList<>();
		for (int r = 0; r < 8; r++)
		{
			for (int c = 0; c < 8; c++)
			{
				if (isEmpty(Position.get(c, r)))
					continue;
				if (getPiece(Position.get(c, r)).getColor() == color)
				{
					moves.addAll(MoveHelper.getAllMoves4Piece(this, Position.get(c, r), false));
				}
			}
		}
		return moves;
	}

	public ArrayList<Move> getAllPossibleMovesWithDefend(int color)
	{
		ArrayList<Move> moves = new ArrayList<>();
		for (int r = 0; r < 8; r++)
		{
			for (int c = 0; c < 8; c++)
			{
				if (isEmpty(Position.get(c, r)))
					continue;
				if (getPiece(Position.get(c, r)).getColor() == color)
				{
					moves.addAll(MoveHelper.getAllMoves4Piece(this, Position.get(c, r), true));
				}
			}
		}
		return moves;
	}

	public int getNumPieces(int color)
	{
		int count = 0;
		for (int r = 0; r < 8; r++)
		{
			for (int c = 0; c < 8; c++)
			{
				if (isEmpty(Position.get(c, r)))
					continue;
				if (getPiece(Position.get(c, r)).getColor() == color)
					count++;
			}
		}
		return count;
	}

	public boolean isCheck(int kingColor)
	{
		for (Move m : getAllPossibleMovesWithoutValidation(Piece.getOppositeColor(kingColor)))
		{
			if (m.getKilledPiece() != null && m.getKilledPiece().getType() == Piece.KING)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isCheckMate(int kingColor)
	{
		if (isCheck(kingColor))
		{
			if (getAllPossibleMovesWithValidation(kingColor).size() == 0)
				return true;
		}
		return false;
	}

	private ArrayList<Move> getAllPossibleMovesWithValidation(int kingColor)
	{

		return null;
	}

	public boolean hasKingMoved(int color)
	{
		if (color == Piece.BLACK)
		{
			return BitField.getBit(blackFlags, KING_MOVED_FLAG);
		} else
		{
			return BitField.getBit(whiteFlags, KING_MOVED_FLAG);
		}
	}

	public boolean hasLRookMoved(int color)
	{
		if (color == Piece.BLACK)
		{
			return BitField.getBit(blackFlags, L_ROOK_FLAG);
		} else
		{
			return BitField.getBit(whiteFlags, L_ROOK_FLAG);
		}
	}

	public boolean hasRRookMoved(int color)
	{
		if (color == Piece.BLACK)
		{
			return BitField.getBit(blackFlags, R_ROOK_FLAG);
		} else
		{
			return BitField.getBit(whiteFlags, R_ROOK_FLAG);
		}
	}

	public void setHasKingMoved(int color, boolean moved)
	{
		if (color == Piece.BLACK)
		{
			blackFlags=(byte) BitField.changeBit(blackFlags, KING_MOVED_FLAG, moved);
		} else
		{
			whiteFlags=(byte) BitField.changeBit(whiteFlags, KING_MOVED_FLAG, moved);
		}
	}

	public void setHasLRookMoved(int color, boolean moved)
	{
		if (color == Piece.BLACK)
		{
			blackFlags=(byte) BitField.changeBit(blackFlags, L_ROOK_FLAG, moved);
		} else
		{
			whiteFlags=(byte) BitField.changeBit(whiteFlags, L_ROOK_FLAG, moved);
		}
	}

	public void setHasRRookMoved(int color, boolean moved)
	{
		if (color == Piece.BLACK)
		{
			blackFlags=(byte) BitField.changeBit(blackFlags, R_ROOK_FLAG, moved);
		} else
		{
			whiteFlags=(byte) BitField.changeBit(whiteFlags, R_ROOK_FLAG, moved);
		}
	}
}
