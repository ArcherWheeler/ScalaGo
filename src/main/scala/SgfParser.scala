import java.io.{PrintWriter, File}

import scala.io.Source

/**
 * Created by Archer Wheeler on 3/26/16.
 */

class GameLog(size: Int, moves: List[(Int,Int,Int)], _winner: Int) {
//  val board = Array.fill[Int]((size*size)+1)(0)
  val board = {
    var b: Board = new Board(1, Array.ofDim[Int](size,size))
    for (
      (p,x,y) <- moves
    ){
      b = b.placeStone(x,y).get
    }
    b
  }
  val winner = _winner

  def finalState: Array[Int] = {
    board.board.flatten ++ Array(winner)
  }
}

object SgfParser {
  val size = 9
  val traingSetDir = """/Users/archerwheeler/Desktop/GoGoDWinter2015/Database/Non19x19Boards/9x9_Games"""
  val writeDir = """/Users/archerwheeler/Desktop/small.txt"""

  def main(args: Array[String]): Unit = {

    val files = getListOfFiles(traingSetDir)
    var games: List[Array[Int]] = List()

    for (file <- files) {
      try {
        val f = Source.fromFile(file.getPath)
        val log = extractGameLog(f.getLines().mkString)
        f.close()
//        (log.board,log.winner)
        games = log.finalState :: games
      } catch {
        case e: java.nio.charset.MalformedInputException => println("BAD!")
        case b: Exception => println("AlsoBAD!")
      }
    }

    println(games.length)

    val writer = new PrintWriter(new File("test.txt" ))
    for(g <- games){
      writer.write(g.mkString(","))
      writer.write("\n")
    }
    writer.close()
  }

  def playerValue(s: String): Int = s match {
    case "B" => 1
    case "W" => 0
  }

  def extractGameLog(sgf: String): GameLog ={
    val moveRE = ";(B|W)\\[([a-z])([a-z])\\]".r
    val winnerRE = "RE\\[(B|W)\\+[0-9.R]+\\]".r

    val win = winnerRE.findFirstIn(sgf)
    val winnerRE(winner) = win.getOrElse(throw new Exception("No game result marked in SGF"))

    val moves = for (p <- moveRE.findAllMatchIn(sgf).toList)
    yield {
      val moveRE(player, x, y) = p
      (playerValue(player),x.charAt(0).toInt - 97,y.charAt(0).toInt - 97)
    }

    new GameLog(size, moves, playerValue(winner))
  }

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    val e = d.exists
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).filter(_.getPath.endsWith(".sgf")).toList
    } else {
      List[File]()
    }
  }
}
