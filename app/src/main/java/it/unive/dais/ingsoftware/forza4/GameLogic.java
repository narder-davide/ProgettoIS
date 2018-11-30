package it.unive.dais.ingsoftware.forza4;

import it.dais.forza4.R;

public class GameLogic{

    final int ROWS = 6;
    final int COLS = 7;
    char[][] matrix;
    int[] quote;    // vettore lungo tanto quanto il numero di colonne.
                    // Contiene l'indice della prima riga disponibile in cui andare ad inserire un gettone.
    String lastGame;

    // Costruttore
    public GameLogic(String lastGame){
        matrix = new char[ROWS][COLS];
        quote = new int[COLS];
        this.lastGame = lastGame;
    }

    /*private void initializeStructures(){
        int i, j, k;

        // initialize matrix
        k = 0;
        for(i=0;i<ROWS;i++){
            for(j=0;j<COLS;j++){
                this.matrix[i][j] = lastGame.charAt(k);
                k++;
            }
        }

        // initialize quote
        for(j=0;j<COLS;j++){
            quote[j] = 0;
            for(i=0;i<ROWS;i++){
                if (lastGame.charAt(i*COLS) != 'X'){
                    quote[j]++;
                }
            }
        }
    }*/

    // Legge la stringa lastGame e carica la matrice corrispondente
    // X -> empty (no coins)
    // Y -> yellow coin
    // R -> red coin
    public void initializeGame(){
        if (lastGame.compareTo("") == 0){
            this.resetGame();
        }
        else {
            int row = 0;
            int col = 0;
            int j;

            for(j=0;j<COLS;j++) {
                quote[j] = 0;
            }

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

        // Resetta matrice
        for(i=0;i<ROWS;i++){
            for(j=0;j<COLS;j++){
                this.matrix[i][j] = 'X';
            }
        }

        // Resetta quote
        for(j=0;j<COLS;j++){
            this.quote[j] = 0;
        }
    }
}
