/**
 * Created by Archer Wheeler on 2/28/16.
 */
trait GameBoard {

  def size: Int
  def placeStone(x: Int, y: Int): Option[GameBoard]
  def getRandomMove: Option[GameBoard]
  def scoreBlack: Int
  def scoreWhite: Int = size*size - scoreBlack
  def board: Array[Array[Int]]

}