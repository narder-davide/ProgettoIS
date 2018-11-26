package it.unive.dais.ingsoftware.forza4;

public class GameLogic {
    final int col=7,row=6;
    int[][] matrix=new int[row][col];

    public void addToken(int r, int c, int color){  //r=riga c=colonna color=0(trasparente) color=1(giallo) color=2(arancione)
        this.matrix[r][c]=color;
    }

    public void removeAllToken(){
        int i,j;
        for(i=0;i<row;i++){
            for(j=0;j<col;j++){
                this.matrix[i][j]=0;
            }
        }
    }
}
