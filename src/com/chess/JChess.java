package com.chess;

import com.chess.engine.board.Board;
import com.chess.gui.Table;

public class JChess {

    public static void main(String[] args) {
        Board b = Board.createStandardBoard();
        System.out.println(b);

        Table table = new Table();
    }
}
