/**
 * Created by Archer Wheeler on 2/1/16.
 */

import scala.collection.mutable.Queue
import scala.util.Random


object Board{
  val r: Random = new Random()
}

class Board(_currentPlayer: Int = 1, _board: Array[Array[Int]],
            lastMove: Option[(Int,Int)] = None, ko: Option[(Int,Int)] = None) {

  val board = _board
  val size = board.size
  val currentPlayer = _currentPlayer
  val opponent = (-1)*_currentPlayer

  def getColor(p: (Int,Int)): Int = {
    board(p._1)(p._2)
  }

  def onBoard(p: (Int,Int)): Boolean = {
    val (x,y) = p
    (x >= 0) && (x < size) && (y >= 0) && (y < size)
  }

  def getAdjacent(p: (Int,Int)): List[(Int,Int)] = {
    val (x,y) = p
    val adjacent = List((x-1,y),(x+1,y),(x,y-1),(x,y+1))
    adjacent.filter(onBoard)
  }

  def getSurrounding(p: (Int,Int)): List[(Int,Int)] = {
    val (x,y) = p
    val surrounding = List((x-1,y-1),(x-1,y),(x-1,y+1),
      (x,y-1),(x,y+1),
      (x+1,y-1),(x+1,y),(x+1,y+1))
    surrounding.filter(onBoard)
  }

  def IsEye(p: (Int,Int)): Boolean = IsEyeOfColor(1)(p) || IsEyeOfColor(-1)(p)

  def ponnuki(color: Int)(p: (Int,Int)): Boolean = {
    getAdjacent(p).forall(p => getColor(p) == color)
  }

  def controled(color: Int)(p: (Int,Int)): Boolean = {
    (getColor(p) == color) || ponnuki(color)(p)
  }

  def IsEyeOfColor(color: Int)(p: (Int,Int)): Boolean = {
    val adj = getAdjacent(p)
    val surrounding = getSurrounding(p)


    ponnuki(color)(p) &&
      ((surrounding.count(controled(color)) == 7)
      || surrounding.forall(controled(color)))

//    // all adjacent are right color
//    // and Either 7 are right color, or all right color
//    adj.count( p => getColor(p) == color) == 4 &&
//      (surrounding.count( p => getColor(p) == color) == 7
//      || surrounding.count( p => getColor(p) == color) == surrounding.size)
//
//
  }

  def toKillIfDead(x: Int, y: Int): List[(Int,Int)] = {
    val color = board(x)(y)
    var search = Queue((x,y))
    var searched: Set[(Int,Int)] = Set()

    while(search.nonEmpty){

      //Is there a more idiomatic scala way?
      var stone = search.dequeue()

      if (!searched.contains(stone)){
        for( (a, b) <- getAdjacent(stone)){

          if (board(a)(b) == 0)
            return List()
          if (board(a)(b) == color)
            search.enqueue((a,b))
        }
      }
      searched += stone
    }
    searched.toList
  }

  //Doesn't seem to be a build in way to do this
  def cloneBoard: Array[Array[Int]] = {
    var newBoard = Array.ofDim[Int](size,size)
    for (x <- 0 until size; y <- 0 until size){
      newBoard(x)(y) = board(x)(y)
    }
    newBoard
  }

  def placeStone(x: Int, y: Int): Option[Board] = {
    // Easy cases to know not legal move
    if ((board(x)(y) != 0) || ko.contains((x,y)) || IsEye(x,y))
      return None

    val nextBoard = cloneBoard

    nextBoard(x)(y) = currentPlayer

//    for ((a,b) <- getAdjacent(x,y)){
//      toKillIfDead(a,b).foreach(p => nextBoard(p._1)(p._2) = 0 )
//    }

    val result = new Board(opponent, nextBoard, lastMove = Some(x,y), ko = lastMove)

    for ((a,b) <- result.getAdjacent(x,y)){
      result.toKillIfDead(a,b).foreach(p => result.board(p._1)(p._2) = 0 )
    }

    //If still surrounded, then self capture is an illegal move
    val e = result.getAdjacent(x,y)
    if (result.toKillIfDead(x,y).nonEmpty)
      return None

    Some(result)
  }

  def getLegalMoves: List[Board] = {

    var boards: List[Board] = List()
    for (x <- 0 until size; y <- 0 until size; b <- placeStone(x,y)){
      boards = b :: boards
    }

    boards
  }


  // THIS IS TERRIBLE SCALA DUDE!!!
  def getRandomMove(): Option[Board] = {
    var emptySpaces = for {
      x <- 0 until size
      y <- 0 until size
      if 0 == board(x)(y)
    } yield (x,y)

    emptySpaces = Board.r.shuffle(emptySpaces)

    while(emptySpaces.nonEmpty){
      val p = emptySpaces.headOption
      val b = placeStone(p.get._1, p.get._2)
      if (b.nonEmpty) return b
      else emptySpaces = emptySpaces.tail
    }

    None
//    for{h <- emptySpaces.headOption
//        b <- placeStone(h._1,h._2)} yield b
  }

  def score: Int = {
    for (x <- 0 until size; y <- 0 until size){
      if (controled(-1)(x,y))
        board(x)(y) = -1

      if(controled(1)(x,y))
        board(x)(y) = 1
    }

    var sum = 0
    for (ar <- board; i <- ar){ sum = i + sum }
    sum
  }


  def seeIt(): Unit = {
    for (y <- 0 until size){
      for(x <- 0 until size){
        val v = board(x)(y)
        if (v == 0) print ("-")
        if (v == 1) print ("X")
        if (v == -1) print ("0")
      }
      println
    }
  }
}









