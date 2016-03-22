import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Archer Wheeler on 2/24/16.
 */
public class JBitBoard {

    Random r = new Random();

    // Used for fast implementation of a Go board
    private class BitMask {
        private BitSet arr;
        private int size;

        private BitMask(int size){
            this.arr = new BitSet(size*size);
            this.size = size;
        }

        private BitMask(BitSet arr, int size){
            this.arr = arr;
            this.size = size;
        }

        private void set(int x, int y, boolean value) {
            if (x >= 0 && x < size && y >= 0 && y < size){
                arr.set((y * size) + x);
            }
        }

        private void merge(BitMask mask){
            arr.or(mask.arr);
        }

        private void shave(BitMask mask){
            arr.andNot(mask.arr);
        }

        private boolean get(int x, int y){
            return arr.get((y * size) + x);
        }

        public BitMask clone() {
            return new BitMask((BitSet) arr.clone(), size);
        }

        public void seeIt(){
            for(int i = 0; i < size; i++){
                for(int j = 0; j < size; j++){
                    if (get(i, j)){
                        System.out.print("x");
                    } else System.out.print("-");
                }
                System.out.println();
            }

            System.out.println("%%%%%%%%%%%%%%%%%%%");
        }

    }

    // Pojo for group bitmasks
    private class BitGroup {
        private BitMask stones;
        private BitMask liberties;
        private BitMask edge;
        private boolean alive;
        private int size;

        private BitGroup(BitMask stones, BitMask liberties, BitMask edge, int size) {
            this.stones = stones;
            this.liberties = liberties;
            this.edge = edge;
            this.size = size;
            this.alive = false;
        }

        public BitGroup copy() {
            return new BitGroup(stones.clone(), liberties.clone(), edge.clone(), size);
        }

        private BitGroup(int size){
            this(new BitMask(size), new BitMask(size), new BitMask(size), size);
        }

        private void status(){
            System.out.println("Stones");
            stones.seeIt();
            System.out.println("Liberties");
            liberties.seeIt();
            System.out.println("Edge");
            edge.seeIt();
        }
    }

    // 1 is Black and -1 is White
    int currentPlayer;

//    private BitMask legalMoves;

    private BitMask allStones;

    private BitGroup playerStones;
    private ArrayList<BitGroup> playerGroups;

    private BitGroup opponentStones;
    private ArrayList<BitGroup> opponentGroups;

    public JBitBoard(int size){
        this.size = size;
        allStones = new BitMask(size);

        playerStones = new BitGroup(size);

        playerGroups = new ArrayList<>();

        opponentStones = new BitGroup(size);

        opponentGroups = new ArrayList<>();

//        legalMoves = new BitMask(size);
//        legalMoves.arr.set(0,size*size, true);

        currentPlayer = 1;
    }

    public JBitBoard(int[][] arrBoard){
        this(arrBoard.length);

        for (int i = 0; i < arrBoard.length; i++){
            for (int j = 0; j < arrBoard.length; j++ ){
                if (arrBoard[i][j] == currentPlayer){
                    placeStone(i,j);
                }
                if (arrBoard[i][j] == -1*currentPlayer){
                    swapPlayers();
                    placeStone(i,j);
                }
            }
        }
    }

    public JBitBoard copy(){
        JBitBoard copy = new JBitBoard(size);
        copy.allStones = allStones.clone();
        copy.currentPlayer = currentPlayer;
        copy.playerStones = playerStones.copy();
        copy.opponentStones = opponentStones.copy();

        ArrayList<BitGroup> pg = new ArrayList<>();
        for (BitGroup bg : playerGroups){
            pg.add(bg);
        }
        copy.playerGroups = pg;

        ArrayList<BitGroup> og = new ArrayList<>();
        for (BitGroup bg : opponentGroups){
            og.add(bg);
        }
        copy.opponentGroups = og;
        return copy;
    }

    public int size(){
        return size;
    }

    private boolean isEye(int x, int y, BitMask board){
        BitMask eye = new BitMask(size);

        eye.set(x-1,y, true);
        eye.set(x+1,y, true);
        eye.set(x,y-1, true);
        eye.set(x,y+1, true);

        boolean onEdge = (eye.arr.cardinality() < 4);
        eye.shave(board);

        // All adjacent must be same color
        if(eye.arr.cardinality() != 0) return false;

        eye.set(x-1,y-1, true);
        eye.set(x+1,y+1, true);
        eye.set(x+1,y-1, true);
        eye.set(x-1,y+1, true);

        eye.shave(board);

        return (eye.arr.cardinality() == 0 && onEdge) || (eye.arr.cardinality() <= 1 && !onEdge);
    }

    private void swapPlayers(){
        ArrayList<BitGroup> holdG = playerGroups;
        playerGroups = opponentGroups;
        opponentGroups = holdG;

        BitGroup holdS = playerStones;
        playerStones = opponentStones;
        opponentStones = holdS;
        currentPlayer = -1*currentPlayer;
    }


    // This would be a lot nicer in Java 8
    private boolean selfCapture(int x, int y){
        for (BitGroup bg : opponentGroups){
            if (bg.liberties.get(x,y) && (bg.liberties.arr.cardinality() == 1)) {
                return false;
            }
        }
        for(BitGroup bg : playerGroups){
            if (bg.liberties.get(x,y) && (bg.liberties.arr.cardinality() > 1)) {
                return false;
            }
        }
        return true;
    }

    int size;


    public boolean legalMove(int x, int y){
        // already stone there
        if (allStones.get(x,y) || isEye(x,y, playerStones.stones)){
            return false;
        }

        BitMask stone = new BitMask(size);
        stone.set(x, y, true);

        // create liberties
        BitMask edge = new BitMask(size);
        edge.set(x-1,y, true);
        edge.set(x+1,y, true);
        edge.set(x,y-1, true);
        edge.set(x, y + 1, true);

        BitGroup newGroup = new BitGroup(stone, edge, edge.clone(), size);

        //clear liberties if on top of stones
        newGroup.liberties.shave(opponentStones.stones);
        newGroup.liberties.shave(playerStones.stones);

        if(newGroup.liberties.arr.isEmpty() && selfCapture(x,y)){
            return false;
        }
        return true;
    }


    /**
     * Modifies board by placing stone.
     * @param x x-coordinate
     * @param y y-coordinate
     * @return True if valid move
     */
    public boolean placeStone(int x, int y){

        // already stone there
        if (allStones.get(x,y) || isEye(x,y, playerStones.stones)){
            return false;
        }

        BitMask stone = new BitMask(size);
        stone.set(x, y, true);

        // create liberties
        BitMask edge = new BitMask(size);
        edge.set(x-1,y, true);
        edge.set(x+1,y, true);
        edge.set(x,y-1, true);
        edge.set(x, y + 1, true);

        BitGroup newGroup = new BitGroup(stone, edge, edge.clone(), size);

        //clear liberties if on top of stones
        newGroup.liberties.shave(opponentStones.stones);
        newGroup.liberties.shave(playerStones.stones);

        if(newGroup.liberties.arr.isEmpty() && selfCapture(x,y)){
            return false;
        }

        // Stone expands existing group
        if (playerStones.liberties.get(x,y)){
            for(BitGroup bg : playerGroups){
                if (bg.liberties.get(x,y)){
                    newGroup.stones.merge(bg.stones);
                    newGroup.liberties.merge(bg.liberties);
                    newGroup.edge.merge(bg.edge);
                }
            }
            newGroup.liberties.shave(newGroup.stones);
            newGroup.edge.shave(newGroup.stones);

            // Delete groups that got merged
            Iterator<BitGroup> i = playerGroups.iterator();
            while(i.hasNext()){
                BitGroup bg = i.next();
                if (bg.liberties.get(x,y)){
                    i.remove();
                }
            }
        }
        playerGroups.add(newGroup);

        //clear white dead stones
        boolean killedStones = false;

        Iterator<BitGroup> i = opponentGroups.iterator();
        while(i.hasNext()){
            BitGroup bg = i.next();
            bg.liberties.shave(newGroup.stones);
            if (bg.liberties.arr.isEmpty()){
                i.remove();
                killedStones = true;
            }
        }


        // rebuild stones from groups
        //====
        BitMask newBStones = new BitMask(size);
        BitMask newBLib = new BitMask(size);
        for(BitGroup bg : playerGroups){
            newBStones.merge(bg.stones);
            newBLib.merge(bg.liberties);
        }
        playerStones = new BitGroup(newBStones, newBLib, new BitMask(size), size);


        BitMask newWStones = new BitMask(size);
        BitMask newWLib = new BitMask(size);
        for(BitGroup bg : opponentGroups){
            newWStones.merge(bg.stones);
            newWLib.merge(bg.liberties);
        }
        opponentStones = new BitGroup(newWStones, newWLib, new BitMask(size), size);

        allStones.arr.clear();
        allStones.merge(opponentStones.stones);
        allStones.merge(playerStones.stones);
        //====

        //reclaim liberties
        if(killedStones){
            for(BitGroup bg : playerGroups){
                bg.liberties = bg.edge.clone();
                bg.liberties.shave(opponentStones.stones);
            }
        }

        swapPlayers();
        return true;
    }

    public void seeIt(){
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if (playerStones.stones.get(i, j)){
                    System.out.print((currentPlayer > 0) ? "x" : "O");
                }else if(opponentStones.stones.get(i,j)) {
                    System.out.print((currentPlayer < 0) ? "x" : "O");
                } else System.out.print("-");
            }
            System.out.println();
        }
    }

    public int blackScore(){
        BitMask score = new BitMask(size);
        score.merge(playerStones.stones);
        score.merge(playerStones.liberties);
        int playerScore = score.arr.cardinality();

        return (currentPlayer > 0) ? playerScore : ((size*size) - playerScore);
    }

    public double rollout(int numRolls){
        double totalScore = 0.0;

        for(int i = 0; i < numRolls; i++){
            JBitBoard b = copy();

            boolean last = true;
//            boolean move = true;
//            while (move || last){
//                move = b.playRandomMove();
//                if (!move) b.swapPlayers();
//                last = move;
//            }
            for (int j = 0; j < (3*(b.size*b.size)); j++){
                boolean move = b.playRandomMove();
                if (!move) b.swapPlayers();
                if (!move && !last) break;
                last = move;
            }
            totalScore += ((double) b.blackScore()) / ( (double) (b.size*b.size));

        }
        return totalScore / numRolls;
    }

    public boolean playRandomMove(){

        ArrayList<Integer> arr = new ArrayList<>();
        for(int k= 0; k < size*size; k++){
            arr.add(k);
        }
        Collections.shuffle(arr);


        boolean playedMove = false;

        for(int i : arr){
            int p = allStones.arr.nextClearBit(arr.get(i));
            int x = p % size;
            int y = p / size;
            if (legalMove(x,y)){
                placeStone(x, y);
                playedMove = true;
                break;
            }
        }

        return playedMove;
    }

    public static void main(String[] args){
        JBitBoard b = new JBitBoard(9);
        b.placeStone(4,3);
        b.placeStone(0,0);
        b.placeStone(3,4);
        b.placeStone(0,1);
        b.placeStone(5,4);
        b.placeStone(0,2);
        b.placeStone(4,5);
        b.placeStone(8,8);
        System.out.println(b.rollout(10000));
        b.seeIt();

        System.out.println("");
        System.out.println("%%%%%%%%%%%%%%%%%%");
        System.out.println("");

        b = new JBitBoard(9);
        b.placeStone(4,4);
        b.swapPlayers();
        System.out.println(b.rollout(10000));
        b.seeIt();

        System.out.println("");
        System.out.println("%%%%%%%%%%%%%%%%%%");
        System.out.println("");


        b = new JBitBoard(9);
        b.placeStone(0,0);
        b.swapPlayers();
        System.out.println(b.rollout(10000));
        b.seeIt();

        System.out.println("");
        System.out.println("%%%%%%%%%%%%%%%%%%");
        System.out.println("");

        b = new JBitBoard(9);
        b.placeStone(2,2);
        b.swapPlayers();
        System.out.println(b.rollout(10000));
        b.seeIt();

        System.out.println("");
        System.out.println("%%%%%%%%%%%%%%%%%%");
        System.out.println("");

        b = new JBitBoard(9);
        b.placeStone(0,1);
        b.swapPlayers();

        b.placeStone(1,0);
        b.swapPlayers();

        b.placeStone(1,1);
        b.swapPlayers();

        b.placeStone(4,4);
        b.swapPlayers();

        System.out.println(b.rollout(10000));
        b.seeIt();

    }

}