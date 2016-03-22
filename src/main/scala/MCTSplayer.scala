import scala.util.Random

/**
 * Created by Archer Wheeler on 2/7/16.
 */

object BoardTree{
  val r: Random = new Random()
  val epsilon = 1e-6
  val r2 = math.sqrt(2)
}

class BoardTree(_board: Board, _depth: Int){
  var children: List[BoardTree] = List()

  def isLeaf: Boolean = children.isEmpty
  def board = _board
  val depth = _depth

  def expand(): Unit = {
    children = for {b <- board.getLegalMoves} yield { new BoardTree(b, depth+1)  }
  }

  var totValue: Double = 0.0
  var numVisits: Double = 0.0
  def value = if (numVisits == 0.0) 0.0
              else totValue / numVisits

  def selectBestChild: BoardTree = {
    val UtcVals= for (child <- children)
      yield {
          child.value + (BoardTree.r2*math.sqrt(math.log(numVisits) / (child.numVisits + 1)))
          // small random number to break ties randomly in unexpanded nodes
        + BoardTree.r.nextDouble() * BoardTree.epsilon
      }
    children.zip(UtcVals).maxBy(_._2)._1
  }

  def updateStats(wins: Double): Unit = {
    numVisits += 1
    totValue += wins
  }

}


object MCTSplayer {

  val epsilon = 1e-6
  val NUM_ROLLOUTS = 100
  val NUM_EXPANSIONS = 1000

  val r: Random = new Random()
  val rollOutHeuristic = RollOut


  def playMove(b: Board): Board = {
    val root = new BoardTree(b, 0)
    for(n <- 0 to NUM_EXPANSIONS){
      expandTree(root)
//      println("expansion: " + n)
    }

    root.children.maxBy(_.value).board
  }

  def expandTree(root: BoardTree): Unit = {
    var node = root
    var visited: List[BoardTree] = List()

    while(!node.isLeaf){
      node = node.selectBestChild
      visited = node :: visited
    }
    node.expand()

    var wins = 0.0

    //if it's still a leaf, then we've expanded to a end board
    if (node.isLeaf) {
      if (node.board.score > 0) wins = NUM_ROLLOUTS
      else wins = 0

    } else {

      val newNode = node.selectBestChild
      wins = rollout(newNode.board)
//      println("depth: " + newNode.depth)
    }

    for(v <- visited){
      v.updateStats(wins)
    }
  }


  def rollout(b: Board): Double = {
    rollOutHeuristic.heuristic(b)
    }
}

object quickTest {
  def main(args: Array[String]): Unit = {
    var board = new Board(1, Array.ofDim[Int](5,5))


    var move = board

    for(n <- 1 to 120){
      if ((n % 2) == 1) {
        move = MCTSplayer.playMove(move)
      } else move = move.getRandomMove().get
      move.seeIt()
      println()
      println("---------")
      println()
    }
  }
}