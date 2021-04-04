package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import static com.chess.engine.board.Move.*;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATES = {-17, -15, -10, -6, 6, 10, 15, 17};

    public Knight(final Alliance pieceAlliance, final int piecePosition) {
        super(PieceType.KNIGHT, piecePosition, pieceAlliance, true);
    }

    public Knight(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.KNIGHT, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public List<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>(8);

        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            int candidateDestinationCoordinate = this.piecePosition + currentCandidateOffset;
            if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {

                if (isFirstColumnExclusion(this.piecePosition, currentCandidateOffset)
                        || isSecondColumnExclusion(this.piecePosition, currentCandidateOffset)
                        || isSeventhColumnExclusion(this.piecePosition, currentCandidateOffset)
                        || isEighthColumnExclusion(this.piecePosition, currentCandidateOffset)) {
                    continue;
                }

                final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);

                if (!candidateDestinationTile.isTileOccupied()) {
                    legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                } else {
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();
                    if (this.pieceAlliance != pieceAlliance) {
                        legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public Knight movePiece(Move move) {
        return new Knight(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    private static boolean isFirstColumnExclusion(
            final int currentPosition, final int canadidateOffset) {
        return BoardUtils.FIRST_COLUMN[currentPosition]
                && ((canadidateOffset == -17)
                        || (canadidateOffset == -10)
                        || canadidateOffset == 6
                        || canadidateOffset == 15);
    }

    private static boolean isSecondColumnExclusion(
            final int currentPosition, final int canadidateOffset) {
        return BoardUtils.SECOND_COLUMN[currentPosition]
                && ((canadidateOffset == -10) || canadidateOffset == 6);
    }

    private static boolean isSeventhColumnExclusion(
            final int currentPosition, final int canadidateOffset) {
        return BoardUtils.SEVENTH_COLUMN[currentPosition]
                && ((canadidateOffset == -6) || canadidateOffset == 10);
    }

    private static boolean isEighthColumnExclusion(
            final int currentPosition, final int canadidateOffset) {
        return BoardUtils.EIGHTH_COLUMN[currentPosition]
                && ((canadidateOffset == -15)
                        || canadidateOffset == -6
                        || canadidateOffset == 10
                        || canadidateOffset == 17);
    }
}
