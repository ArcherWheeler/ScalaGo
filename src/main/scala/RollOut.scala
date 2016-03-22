import akka.actor.Actor

/**
 * Created by Archer Wheeler on 3/1/16.
 */

object RollOut{
  val NUM_ROLLOUTS = 100

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
case class Position(board: Board, player: Int) extends Message
case class Evaluation(value: Double) extends Message

abstract class Heuristic(weight: Double) extends Actor {

  def heuristic(board: Board, player: Int): Double
  def receive = {
    case Position(board, player) =>
      sender ! Evaluation(heuristic(board, player))
  }
}