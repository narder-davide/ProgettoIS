package it.unive.dais.ingsoftware.forza4;

import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Random;

import it.dais.forza4.R;

public class GameLogic{

    private final int MAX_COIN = 21;

    private int userCoin = MAX_COIN;
    private int robotCoin = MAX_COIN;

    final int ROWS = 6;
    final int COLS = 7;
    char[][] matrix;
    int[][] voto;
    Long nodes;
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
        nodes = new Long(0);
        checkStrGame();
        resetGame();
    }

    private void checkStrGame(){
        for(int i=0;i<lastGame.length();i++){
            if (lastGame.charAt(i) == 'R'){
                decreaseUserCoin();
            }
            else if (lastGame.charAt(i) == 'Y'){
                decreaseRobotCoin();
            }
        }
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
            if (quote[c] <= ROWS-1) {
                this.quote[c]++;    // Si presuppone che la stringa lastGame sia sempre coerente e valida
            }
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

        // EASY
        if (diff.compareTo("easy") == 0){
            return easyMove('R');
            /*Random random = new Random();
            int c;

            do {
                // r = random.nextInt(ROWS);
                c = random.nextInt(COLS);
            } while(quote[c] >= ROWS-1);

            out = c;*/
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
            return hardMove('Y');
        }
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

        if (userCoin == 0 && robotCoin == 0){
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

    /*** MODALITA' HARD ****/
    private int goodness(char player, int depth, int column, int trigger) {
        int max,i,value,j;

        max = -200;

        if (player == 'R'){
            if (winner() == 'Y') return -128;
        }
        else {
            if (winner() == 'R') return -128;
        }
        // if (winner() == 'R' || winner() == 'Y') return -128;

        if (depth == 0){
            return 0;
        }
        else {
            for (i=0; i<COLS; i++) {
                if (matrix[0][i] == 'X') {
                    j = ROWS - 1;
                    while (matrix[j][i] != 'X') j--;
                    matrix[j][i] = player;
                    nodes++;

                    if (player == 'Y') value = -goodness('R', depth - 1, i, -max) / 2;
                    else value = -goodness('Y', depth - 1, i, -max) / 2;
                    //value = -goodness(player,depth-1,i,-max)/2;

                    matrix[j][i] = 'X';
                    if (value > max) max = value;
                    if (value > trigger) return max;
                }
            }
            return max;
        }
    }

    // UTILIZZARLO PER RITORNARE ANCHE SUGGERIMENTI ALL'UTENTE con un Toast DOPO tot. secondi di inattività
    public int hardMove(char player) {
        int i, j, max, value, best;
        int nodes;
        int[] res = new int[COLS];
        int depth = 6;

        max = -100;
        best = -1;
        for(i=0;i<COLS;i++) {
            if (matrix[0][i] == 'X') {
                nodes = 0;
                j = ROWS-1;
                while((matrix[j][i] != 'X') && (j>=0)) j--;
                matrix[j][i] = player;

                if (player == 'Y') value = -goodness('R',depth, i,200);
                else value = -goodness('Y',depth, i,200);

                Log.i("CAL","\nmove:"+i+1+"    goodness: "+ value +" tree size for this move: "+nodes+" nodes");

                res[i] = value;
                matrix[j][i] = 'X';
                if (value > max) {
                    max = value;
                    best = i;
                }
            }
        }
        if (best == -1) {
            Random random = new Random();
            int c;

            do {
                c = random.nextInt(COLS);
            } while(quote[c] >= ROWS-1);

            best = c;
            /*for(i=0;i<COLS;i++){
                if(matrix[0][i] == 'X') return i;
            }*/
        }

        return best;
    }
    /*** END MODALITA' HARD ****/


    /*** MODALITA' EASY ***/
    // Cerca di ostacolare la giocata dell'avversario posizionando sulle celle di maggior rilievo contigue all'avverario
    // inoltre rileva e cerca di impedire la mossa vincente dell'avversario
    // ritorna -1 se matrix non ha celle disponibili altrimenti il valore della colonna
    public int easyMove(char enemy){
        votazioni(enemy);
        int best_row = 0;
        int best_col = 0;

        for(int i=ROWS-1;i>=0;i--){
            for(int j=0; j<COLS; j++){
                if (voto[i][j] > voto[best_row][best_col]){
                    best_row = i;
                    best_col = j;
                }
            }
        }

        if(matrix[best_row][best_col] != 'X') return -1;
        else return best_col;
    }

    // Ritorna il valore della cella se appartiene a matrix altrimenti 'X'
    private char getCellValue(int row, int col){
        if(row >= ROWS || col >= COLS || row < 0 || col < 0) return 'X';
        else return matrix[row][col];
    }

    // maggiore è il valore del voto maggiore è la priorità della cella
    private void votazioni(char enemy) {

        this.voto = new int[ROWS][COLS];
        int[] cime = new int[COLS];
        int first = 0;
        int second = 0;
        int max = 15;   // valore minimo di una cella vincente per enemy

        // reset voto
        for(int i=0;i<ROWS; i++){
            for(int j=0; j<COLS; j++){
                voto[i][j] = 0;
            }
        }
        // cerco cime
        for(int j=0; j<COLS; j++){
            cime[j] = -1;
            for(int i=0;i<ROWS; i++) {
                if(matrix[i][j] == 'X') cime[j] = i;
            }
        }

        // do voto a ogni possibile cima
        for(int j=0;j<COLS;j++) {

            // se ho esaurito spazio sulla colonna la salto
            int i = 0;
            if(cime[j] < 0 ) continue;
            else i = cime[j];

            int top = i-3;
            int bottom = i+3;
            int left = j-3;
            int right = j+3;

            // controllo gettoni sulla diagonale
            first = 0;
            second = 0;
            for(int r=i-1, c=j-1; r>=top && c>=left && getCellValue(r,c)==enemy; r--, c--) first++;
            for(int r=i+1, c=j+1; r<=bottom && c<=right && getCellValue(r,c)==enemy; r++, c++) second++;
            // se l'avversario può vincere con una mossa, il valore di voto sarà molto alto
            if(first + second >= 3) voto[i][j] = max;
            else voto[i][j] += (first + second);

            // controllo gettoni sull'anti-diagonale
            first = 0;
            second = 0;
            for(int r=i+1, c=j-1; r<=bottom && c>=left && getCellValue(r,c)==enemy; r++, c--) first++;
            for(int r=i-1, c=j+1; r>=top && c<=right && getCellValue(r,c)==enemy; r--, c++) second++;
            // se l'avversario può vincere con una mossa, il valore di voto sarà molto alto
            if(first + second >= 3) voto[i][j] = max;
            else voto[i][j] += (first + second);

            // controllo gettoni orrizzontale
            first = 0;
            second = 0;
            for(int c=j-1; c>=left && getCellValue(i,c)==enemy; c--) first++;
            for(int c=j+1; c<=right && getCellValue(i,c)==enemy; c++) second++;
            // se l'avversario può vincere con una mossa, il valore di voto sarà molto alto
            if(first + second >= 3) voto[i][j] = max;
            else voto[i][j] += (first + second);

            // controllo gettoni verticale
            first = 0;
            for(int r=i+1; r<=bottom && getCellValue(r,j)==enemy; r++) first++;
            // se l'avversario può vincere con una mossa, il valore di voto sarà molto alto
            if(first == 3) first = max;
            voto[i][j] += first;
        }
    }
    /*** END MODALITA' NORMALE ***/

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
}
