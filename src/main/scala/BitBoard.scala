/**
 * Created by Archer Wheeler on 2/28/16.
 */
class BitBoard(_jboard: JBitBoard){

  def this(_size: Int) {
    this(new JBitBoard(_size))
  }

  val jboard: JBitBoard = _jboard

   def size: Int = jboard.size()

   def placeStone(x: Int, y: Int): Option[BitBoard] = {
    if (jboard.legalMove(x,y)) return None

    val r = jboard.copy()
    r.placeStone(x,y)
    Some(new BitBoard(r))
  }

   def getRandomMove: Option[BitBoard] = {
    val r = jboard.copy()
    r.playRandomMove()
    Some(new BitBoard(r))
  }

   def scoreBlack: Int = jboard.blackScore()
}
