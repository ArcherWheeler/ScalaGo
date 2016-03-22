/**
 * Created by Archer Wheeler on 2/20/16.
 */

import java.awt.Color

import scala.swing._
import scala.swing.event.MouseClicked

object GUI {
  def main(args: Array[String]) {
    val ui = new UI
    ui.visible = true
  }
}

class Canvas(_b: Board) extends Component {

  var b = _b
  def d = size
  val n = b.size+1
  def offset = d.height / n
  def stoneLength = 3*offset/4

  override def paintComponent(g : Graphics2D) {

    g.setColor(Color.lightGray)
    g.fillRect(0,0, d.width, d.height)

    for (x <- 1 until n) {
      g.setColor(Color.black)
      g.drawLine(offset*x, offset, offset*x, (n-1)*offset)
    }
    for (y <- 1 until n) {
      g.setColor(Color.black)
      g.drawLine(offset, offset*y, (n-1)*offset, offset*y)
    }

    for (x <- 0 until b.size; y <- 0 until b.size){

      if (b.board(x)(y) == 1){
        g.setColor(Color.black)
        g.fillOval( (x+1)*offset-(stoneLength/2), (y+1)*offset-(stoneLength/2), stoneLength, stoneLength)
      }

      if (b.board(x)(y) == -1){
        g.setColor(Color.white)
        g.fillOval((x+1)*offset-(stoneLength/2), (y+1)*offset-(stoneLength/2), stoneLength, stoneLength)
      }

    }
  }
}


class UI extends MainFrame {
  title = "Scala Go"
  preferredSize = new Dimension(640, 640)
  contents = Button("Press me, please") { println("Thank you") }

  var board = new Board(1, Array.ofDim[Int](7,7))
  var readyForMove = false
  board = MCTSplayer.playMove(board)
  readyForMove = true

  var canvas = new Canvas(board)
  contents = new BorderPanel {
    border = Swing.MatteBorder(8, 8, 8, 8, Color.white)
    add(canvas, BorderPanel.Position.Center)
  }



  listenTo(canvas.mouse.clicks)
  reactions += {
    case e: MouseClicked =>
      val x = e.point.getX
      val y = e.point.getY
      if (readyForMove){
        readyForMove = false
        makeMove(x,y)
      }
  }



  def makeMove(_x: Double,_y: Double): Unit = {
    val x: Int = ((_x - (canvas.stoneLength/2)) / canvas.offset).toInt
    val y: Int = ((_y - (canvas.stoneLength/2)) / canvas.offset).toInt
    println("at coordinate " + x + " " + y)

    if((x >= 0) && (y >= 0) && (board.size >= x) && (board.size >= y)){
      board = board.placeStone(x,y).get
      board.seeIt()
      canvas.b = board
      canvas.repaint()

      board = MCTSplayer.playMove(board)
      canvas.b = board
      canvas.repaint()

      readyForMove = true
    }

  }
}




