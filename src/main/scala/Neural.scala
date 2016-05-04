/**
 * Created by Archer Wheeler on 3/21/16.
 */

import com.typesafe.scalalogging.Logger
import org.canova.api.records.reader.RecordReader
import org.canova.api.records.reader.impl.CSVRecordReader
import org.canova.api.split.FileSplit
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, DenseLayer, OutputLayer, SubsamplingLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.{DataSet, SplitTestAndTrain}
import org.nd4j.linalg.io.ClassPathResource
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.slf4j.LoggerFactory


object Neural{

  val log = Logger(LoggerFactory.getLogger("name"))



  def main(args: Array[String]) {

    val nChannels = 81
    val outputNum = 2
    val nEpochs = 10
    val iterations = 1
    val seed = 123

    //First: get the dataset using the record reader. CSVRecordReader handles loading/parsing
    val numLinesToSkip = 0
    val delimiter = ","
    val recordReader: RecordReader = new CSVRecordReader(numLinesToSkip,delimiter)
    recordReader.initialize(new FileSplit(new ClassPathResource("test.txt").getFile))

    //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
    val labelIndex = 81     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
    val numClasses = 2     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2
    val batchSize = 130    //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)
    val iterator: DataSetIterator = new RecordReaderDataSetIterator(recordReader,batchSize,labelIndex,numClasses)




    val next: DataSet = iterator.next()
    val testAndTrain: SplitTestAndTrain = next.splitTestAndTrain(0.65)

    val mnistTrain = testAndTrain.getTrain
    val mnistTest = testAndTrain.getTest



    log.info("Build model....")
    val builder: MultiLayerConfiguration.Builder = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(iterations)
      .regularization(true).l2(0.0005)
      .learningRate(0.01)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(Updater.NESTEROVS).momentum(0.9)
      .list(4)
      .layer(0, new ConvolutionLayer.Builder(5, 5)
        .nIn(nChannels)
        .stride(1, 1)
        .nOut(9).dropOut(0.5)
        .activation("relu")
        .build())
      .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2,2)
        .stride(2,2)
        .build())
      .layer(2, new DenseLayer.Builder().activation("relu")
        .nOut(9).build())
      .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nOut(outputNum)
        .activation("softmax")
        .build())
      .backprop(true).pretrain(false)

    new ConvolutionLayerSetup(builder,9,9,1)

    val conf: MultiLayerConfiguration = builder.build()

    val model: MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()


    log.info("Train model....")
    model.setListeners(new ScoreIterationListener(100))
    (0 until nEpochs).foreach {
      i => model.fit(mnistTrain)
      val eval = new Evaluation(2)
      val test: DataSet = testAndTrain.getTest
      val output: INDArray = model.output(test.getFeatureMatrix)
      eval.eval(test.getLabels, output)
      log.info(eval.stats())
    }


  }

}
