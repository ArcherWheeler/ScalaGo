import akka.actor.Actor

/**
 * Created by Archer Wheeler on 3/1/16.
 */

object RollOut{
  val NUM_ROLLOUTS = 100
  val weight = 1

  def heuristic(board: Board): Double = {
    val jBoard = new JBitBoard(board.cloneBoard)
    val bWins = jBoard.rollout(NUM_ROLLOUTS)
    val player = board.currentPlayer

    player match {
      case 1 => 1-bWins
      case -1 => bWins
    }

  }
}

sealed trait Message
case class Position(node: BoardTree, board: Board, player: Int) extends Message
case class BoardEvaluation(node: BoardTree, value: Double, weight: Double) extends Message
case object BestMove extends Message

abstract class Heuristic(weight: Double) extends Actor {
  def heuristic(board: Board, player: Int): Double

  def receive = {
    case Position(node, board, player) =>
      sender ! BoardEvaluation(node, heuristic(board, player), weight)
  }
}

object GameTree extends Actor {

//  val root: BoardTree

  def receive = {
    case BestMove => {
//      val b = root.selectBestChild
//      sender ! Position(b, b.board, b.board.currentPlayer )
    }

    case BoardEvaluation(node, value, weight) => {
      node.updateStats(value)
    }

  }
}