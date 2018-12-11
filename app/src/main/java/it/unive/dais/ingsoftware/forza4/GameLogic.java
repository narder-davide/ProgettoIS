package it.unive.dais.ingsoftware.forza4;

import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Random;

import it.dais.forza4.R;

public class GameLogic{

    private final int MAX_COIN = 20;

    private int userCoin = MAX_COIN;
    private int robotCoin = MAX_COIN-1;

    final int ROWS = 6;
    final int COLS = 7;
    char[][] matrix;
    int[][] voto;
    int[] quote;    // vettore lungo tanto quanto il numero di colonne.
                    // Contiene l'indice della prima riga disponibile in cui andare ad inserire un gettone.
    TableLayout gameGrid;
    String lastGame;
    int turno;

    // Costruttore
    public GameLogic(TableLayout gameGrid, String lastGame){
        matrix = new char[ROWS][COLS];
        voto = new int[ROWS][COLS];
        quote = new int[COLS];
        this.gameGrid = gameGrid;
        this.lastGame = lastGame;
        this.turno = 0;
        resetGame();
    }

    public int getUserCoin(){
        return this.userCoin;
    }

    public int getRobotCoin(){
        return this.robotCoin;
    }

    public void decreaseUserCoin(){
        this.userCoin--;
    }

    public void decreaseRobotCoin(){
        this.robotCoin--;
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
                this.setCoin(col, lastGame.charAt(i));
                col++;
            }
        }
    }

    // Inserisce un gettone in una posizione specifica
    // X -> empty (no coins)
    // Y -> yellow coin
    // R -> red coin
    public void setCoin(int c, char type){
        //this.matrix[quote[c]][c] = type;
        int r = (ROWS-1)-quote[c];

        this.matrix[r][c] = type;

        if (type != 'X') {
            this.quote[c]++;    // Si presuppone che la stringa lastGame sia sempre coerente e valida
        }

        c = c+(COLS-1)-(2*c);

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

    public int calculateRobotAction(String diff){
        int out = 0;

        // EASY
        if (diff.compareTo("easy") == 0){
            Random random = new Random();
            int c;

            do {
                // r = random.nextInt(ROWS);
                c = random.nextInt(COLS);
            } while(quote[c] >= ROWS-1);

            out = c;
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
            //return this.getBestMove('Y');
            return this.getBestMove();
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
        int red=0, yellow = 0;

        if (userCoin == 0 || robotCoin == 0){
            return 'X';
        }

        // controllo riga
        for(int r=0;r<ROWS;r++){
            for(int c=0;c<COLS;c++){
                //System.out.println(r+"|"+c+"| "+matrix[r][c]+": "+(matrix[r][c]=='R' ? "RedOK" : "RedNo"));
                if(matrix[r][c]=='R'){
                    red++;
                    yellow = 0;

                }
                else if(matrix[r][c]=='Y'){
                    yellow++;
                    red = 0;
                }
                else{
                    red = 0;
                    yellow = 0;
                }
                if(red>=4 || yellow>=4){
                    return red >= 4? 'R' : 'Y';
                }
            }

            red=0;
            yellow=0;
        }

        // controllo colonna
        for(int c=0;c<COLS;c++){
            for(int r=0;r<ROWS;r++){
                if(matrix[r][c]=='R'){
                    red++;
                    yellow = 0;
                }
                else if(matrix[r][c]=='Y'){
                    yellow++;
                    red = 0;
                }
                else{
                    red = 0;
                    yellow = 0;
                }
                if(red>=4 || yellow>=4){
                    return red >= 4? 'R' : 'Y';
                }
            }

            red=0;
            yellow=0;
        }

        // controllo \ (diagonale) partendo dalle cella della prima colonna
        for(int trow = 0; trow<ROWS; trow++) {
            for (int r = trow, c = 0; r < ROWS && c < COLS; r++,c++) {
                if (matrix[r][c] == 'R') {
                    red++;
                    yellow = 0;
                } else if (matrix[r][c] == 'Y') {
                    yellow++;
                    red = 0;
                } else {
                    red = 0;
                    yellow = 0;
                }
                if (red >= 4 || yellow >= 4) {
                    return red >= 4 ? 'R' : 'Y';
                }
            }
            red = 0;
            yellow = 0;
        }

        // controllo / (anti-diagonale) partendo dalle celle dell'ultima colonna
        for(int trow = 0; trow<ROWS; trow++) {
            for (int r = trow, c = COLS-1; r < ROWS && c >= 0; r++,c--) {
                if (matrix[r][c] == 'R') {
                    red++;
                    yellow = 0;
                } else if (matrix[r][c] == 'Y') {
                    yellow++;
                    red = 0;
                } else {
                    red = 0;
                    yellow = 0;
                }
                if (red >= 4 || yellow >= 4) {
                    return red >= 4 ? 'R' : 'Y';
                }
            }
            red = 0;
            yellow = 0;
        }

        // controllo \ partendo dalle celle della prima riga
        for(int tcol = 0; tcol<COLS; tcol++) {
            for (int r = 0, c = tcol; r < ROWS && c < COLS; r++,c++) {
                if (matrix[r][c] == 'R') {
                    red++;
                    yellow = 0;
                } else if (matrix[r][c] == 'Y') {
                    yellow++;
                    red = 0;
                } else {
                    red = 0;
                    yellow = 0;
                }
                if (red >= 4 || yellow >= 4) {
                    return red >= 4 ? 'R' : 'Y';
                }
            }
            red = 0;
            yellow = 0;
        }

        // controllo / da ogni cella della prima riga
        for(int tcol = 0; tcol<COLS; tcol++) {
            for (int r = 0, c = tcol; r < ROWS && c >= 0; r++,c--) {
                if (matrix[r][c] == 'R') {
                    red++;
                    yellow = 0;
                } else if (matrix[r][c] == 'Y') {
                    yellow++;
                    red = 0;
                } else {
                    red = 0;
                    yellow = 0;
                }
                if (red >= 4 || yellow >= 4) {
                    return red >= 4 ? 'R' : 'Y';
                }
            }
            red = 0;
            yellow = 0;
        }

        return 'H'; // o 'R' o 'Y' o 'X'=patta o 'H'=continua
    }

    /*private int goodness(char player, int depth, int column, int trigger) {
        int max,i,value,j;
        int nodes;
        max = -200;

        if (winner() == 'R' || winner() == 'Y') return -128;

        if (depth == 0) return 0;

        for(i=0;i<COLS;i++){
            if(matrix[0][i] == 'X') {
                nodes = 0;
                j = ROWS-1;
                while(matrix[j][i] != 'X') j--;
                matrix[j][i] = player;
                nodes++;
                value = -goodness(player,depth-1,i,-max)/2;
                matrix[j][i] = 'X';
                if (value>max) max = value;
                if (value>trigger) return max;
            }
        }
        return max;
    }

    // UTILIZZARLO PER RITORNARE ANCHE SUGGERIMENTI ALL'UTENTE con un Toast DOPO tot. secondi di inattività
    public int getBestMove(char player) {
        int i, j, max, value, best;
        int nodes;
        int[] res = new int[COLS];

        max = -100;
        best = -1;
        for(i=0;i<COLS;i++) {
            if(matrix[0][i] == 'X') {
                nodes = 0;
                j = ROWS-1;
                while((matrix[j][i] != 'X') && (j>=0)) j--;
                matrix[j][i] = player;
                value = -goodness(player,10, i,200);
                Log.i("CAL","\nmove:"+i+1+"    goodness: "+ value +"    tree size for this move: "+nodes+" nodes");
                res[i] = value;
                matrix[j][i] = 'X';
                if (value > max) {
                    max = value;
                    best = i;
                }
            }
        }
        if (best == -1) {
            for(i=0;i<COLS;i++){
                if(matrix[0][i] == 'X') return i;
            }
        }

        return best;
    }*/



    public int getBestMove(){
        votazioni();
        Random random = new Random();
        int colonna = random.nextInt(COLS);

        for(int i=0;i<ROWS;i++){
            for(int k=0;k<COLS;k++){
                if (voto[i][k] > voto[i][colonna]){
                    colonna = k;
                }
            }
        }

        boolean piena = true;
        int i = 5;

        while(piena==true){
            if(i < 0){
                colonna = random.nextInt(COLS);
                i = 5;
            }
            if (matrix[i][colonna] == 'X'){
                piena=false;
            }
            else {
                i--;
            }
        }

        return colonna;
    }

    //algoritmo MIN MAX da implementare a piacimento
    private void votazioni() {

        for(int i=0;i<6;i++){
            for(int k=0;k<7;k++) {

                voto[i][k]=0;

                if(matrix[i-1][k-1]==matrix[i-2][k-2] && matrix[i-1][k-1]==matrix[i-3][k-3]
                        && matrix[i-1][k-1]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i-1][k]==matrix[i-2][k] && matrix[i-1][k]==matrix[i-3][k]
                        && matrix[i-1][k]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i-1][k+1]==matrix[i-2][k+2] && matrix[i-1][k+1]==matrix[i-3][k+3]
                        && matrix[i-1][k+1]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i][k-1]==matrix[i][k-2] && matrix[i][k-1]==matrix[i][k-3]
                        && matrix[i][k-1]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i][k+1]==matrix[i][k+2] && matrix[i][k+3]==matrix[i][k+1]
                        && matrix[i][k+1]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i+1][k-1]==matrix[i+2][k-2] && matrix[i+1][k-1]==matrix[i+3][k-3]
                        && matrix[i+1][k-1]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i+1][k]==matrix[i+2][k] &&matrix[i+1][k]==matrix[i+3][k]
                        && matrix[i+1][k]!=0)
                {
                    voto[i][k]=10;
                }
                if(matrix[i+1][k+1]==matrix[i+2][k+2] && matrix[i+1][k+1]==matrix[i+3][k+3]
                        && matrix[i+1][k+1]!=0)
                {
                    voto[i][k]=10;
                }
            }
        }
    }

    // Legge la matrice e crea una stringa corrispondente alla partita appena interrotta
    public String getLastGame(){
        int i, j;
        String out = "";

        for(i=0;i<ROWS;i++){
            for(j=0;j<COLS;j++){
                out += matrix[i][j];
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
