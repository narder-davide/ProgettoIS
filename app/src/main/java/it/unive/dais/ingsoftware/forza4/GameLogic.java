package it.unive.dais.ingsoftware.forza4;

import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Random;

import it.dais.forza4.R;

public class GameLogic{

    final int ROWS = 6;
    final int COLS = 7;
    char[][] matrix;
    int[] quote;    // vettore lungo tanto quanto il numero di colonne.
                    // Contiene l'indice della prima riga disponibile in cui andare ad inserire un gettone.
    TableLayout gameGrid;
    String lastGame;
    int turno;

    // Costruttore
    public GameLogic(TableLayout gameGrid, String lastGame){
        matrix = new char[ROWS][COLS];
        quote = new int[COLS];
        this.gameGrid = gameGrid;
        this.lastGame = lastGame;
        this.turno = 0;
    }

    // Legge la stringa lastGame e carica la matrice corrispondente
    // X -> empty (no coins)
    // Y -> yellow coin
    // R -> red coin
    public void initializeGame(){
        this.resetGame();

        if (lastGame.compareTo("") != 0){
            int row = 0;
            int col = 0;

            for(int i=0;i<lastGame.length();i++){
                if (col == COLS){
                    col = 0;
                    row++;
                }
                this.setCoin(row, col, lastGame.charAt(i));
                col++;
            }
        }
    }

    // Inserisce un gettone in una posizione specifica
    // X -> empty (no coins)
    // Y -> yellow coin
    // R -> red coin
    public void setCoin(int r, int c, char type){
        this.matrix[r][c] = type;

        if (type != 'X') {
            this.quote[c]++;    // Si presuppone che la stringa lastGame sia sempre coerente e valida
        }

        r = (ROWS-1)-r;

        if (type == 'R') {
            ((TableRow)gameGrid.getChildAt(r)).getVirtualChildAt(c).setBackgroundResource(R.drawable.rounded_button_red);
        }
        else if (type == 'Y'){
            ((TableRow)gameGrid.getChildAt(r)).getVirtualChildAt(c).setBackgroundResource(R.drawable.rounded_button_yellow);
        }
        else {
            ((TableRow)gameGrid.getChildAt(r)).getVirtualChildAt(c).setBackgroundResource(R.drawable.rounded_button_empty);
        }
    }

    public int[] calculateRobotAction(String diff){
        int[] out = new int[2];

        // EASY
        if (diff.compareTo("easy") == 0){
            Random random = new Random();
            int r, c;

            do {
                r = random.nextInt(ROWS);
                c = random.nextInt(COLS);
            } while(quote[c] >= ROWS-1);

            out[0] = r;
            out[1] = c;
        }
        // NORMAL
        else if (diff.compareTo("norm") == 0){
            if (turno%2 == 0){
                return calculateRobotAction("easy");
            }
            else {
                return calculateRobotAction("hard");
            }
        }
        // HARD
        else {

        }

        return out;
    }

    public void incrementTurno(){
        this.turno++;
    }

    // 'R' ha vinto RED
    // 'Y' ha vinto YELLOW
    // 'X' partita patta (gettoni esauriti)
    // 'H' ancora nessun vincitore, continua a giocare
    public char winner(){

        return 'H'; // o 'R' o 'Y' o 'X'=patta
    }

    // Legge la matrice e crea una stringa corrispondente alla partita appena interrotta
    public String getLastGame(){
        int i, j;
        String out = "";

        for(i=0;i<ROWS;i++){
            for(j=0;j<COLS;j++){
                switch (matrix[i][j]) {
                    case 'X':
                        out += "X";
                        break;
                    case 'R':
                        out += "R";
                        break;
                    case 'Y':
                        out += "Y";
                        break;
                    default:
                        out += "X";
                }
            }
        }
        this.lastGame = out;

        return out;
    }

    // Azzeramento della matrice e della griglia di gioco
    public void resetGame(){
        int i,j;

        // Resetta matrice e layout
        for(i=0;i<ROWS;i++){
            for(j=0;j<COLS;j++){
                this.matrix[i][j] = 'X';

                int r = (ROWS-1)-i;
                int c = j;

                ((TableRow)gameGrid.getChildAt(r)).getVirtualChildAt(c).setBackgroundResource(R.drawable.rounded_button_empty);
            }
        }

        // Resetta quote
        for(j=0;j<COLS;j++){
            this.quote[j] = 0;
        }
    }

    public void printMatrix(){
        int i,j;

        System.out.println("MATRIX\n");
        for(i=0;i<ROWS;i++){
            for(j=0;j<COLS;j++){
                System.out.println(this.matrix[i][j]);
            }
            System.out.println("\n");
        }
    }

    public void printQuote(){
        int j;

        System.out.println("QUOTE\n");
        for(j=0;j<COLS;j++){
            System.out.println(this.quote[j]);
        }
    }
}
