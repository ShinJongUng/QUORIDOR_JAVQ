package main;

import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import static main.Board.*;

public class Pawn {
    static final String[] NAMES = {"Player1", "Player2"};
    public static JButton[] Pawns = new JButton[2];
    public static int pawn_Location[][] = new int[2][2];
    public static void setPawn(int turn, int x, int y) {
        if(Pawns[turn] != null) {
            Pawns[turn].setText("");
            Pawns[turn].setBackground(new Color(150, 75, 0));
        }
        Pawns[turn] = Spaces[x][y];
        Pawns[turn].setText(NAMES[turn]);
        Pawns[turn].setBackground(Color.gray);
        pawn_Location[turn][0] = x;
        pawn_Location[turn][1] = y;
    }
}