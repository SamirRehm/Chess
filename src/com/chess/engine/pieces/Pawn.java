package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class Pawn extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATE = {8, 16, 7, 9};

    public Pawn(final Alliance pieceAlliance, final int piecePosition) {
        super(piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int currentCanadidateOffset : CANDIDATE_MOVE_COORDINATE) {
            final int candidateDestinationCoordinate =
                    this.piecePosition
                            + (this.getPieceAlliance().getDirection() * currentCanadidateOffset);

            if (!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }

            if (currentCanadidateOffset == 8
                    && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
            } else if (currentCanadidateOffset == 16
                            && this.isFirstMove()
                            && (BoardUtils.SECOND_ROW[this.piecePosition]
                                    && this.getPieceAlliance().isBlack())
                    || (BoardUtils.SEVENTH_ROW[this.piecePosition]
                            && this.getPieceAlliance().isWhite())) {
                final int behindCandidateDestinationCoordinate =
                        this.piecePosition + (this.pieceAlliance.getDirection() * 8);
                if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied()
                        && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                }
            } else if ((currentCanadidateOffset == 7
                            && !((BoardUtils.EIGHTH_COLUMN[this.piecePosition]
                                            && this.getPieceAlliance().isWhite())
                                    || (BoardUtils.FIRST_COLUMN[this.piecePosition]
                                            && this.pieceAlliance.isBlack())))
                    || (currentCanadidateOffset == 9
                            && !((BoardUtils.EIGHTH_COLUMN[this.piecePosition]
                                            && this.getPieceAlliance().isBlack())
                                    || (BoardUtils.FIRST_COLUMN[this.piecePosition]
                                            && this.pieceAlliance.isWhite())))) {
                final Piece pieceOnCandidate =
                        board.getTile(candidateDestinationCoordinate).getPiece();
                if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                    legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }
}
