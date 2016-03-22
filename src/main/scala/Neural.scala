import kr.ac.kaist.ir.deep.fn.{StochasticGradientDescent, Sigmoid}
import kr.ac.kaist.ir.deep.network.Network
import kr.ac.kaist.ir.deep.train._

/**
 * Created by Archer Wheeler on 3/21/16.
 */
// Define 2 -> 4 -> 1 Layered, Fully connected network.
val net = Network(Sigmoid, 81, 81, 81)
// Define Manipulation Type. VectorType, AEType, RAEType, StandardRAEType, URAEType, and StringToVectorType.
val operation = new VectorType(
  corrupt = GaussianCorruption(variance = 0.1)
)
// Define Training Style. SingleThreadTrainStyle, MultiThreadTrainStyle, & DistBeliefTrainStyle
val style = new SingleThreadTrainStyle(
  net = net,
  algorithm = new StochasticGradientDescent(l2decay = 0.0001f),
  make = operation,
  param = SimpleTrainingCriteria(miniBatchFraction = 0.01f))
// Define Trainer
val train = new Trainer(
  style = style,
  stops = StoppingCriteria(maxIter = 100000))
// Do Train

train.train(set, valid)