package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule


class BimodalPredictor() extends MultiIOModule {
  val io = IO(
    new Bundle{
      val inputAddress = Input(UInt(32.W))
      // Used to update the state of the 2 bit counters
      val inputWasTaken = Input(Bool())
      val updateStateControl = Input(Bool())

      val predictTaken = Output(Bool())
    }
  )

  /*
   * Functionality
   * 1. Make a prediction given an input address (taken or not taken)
   * 2. Update the state of a 2 bit counter given the inputAddress and whether it was taken or not
   *
   * How:
   * - hash the pc using m bits
   * - lookup prediction in table (taken not taken)
   *  - table 2**m entries
   *  if val >= 2 -> taken
   *  if val < 2 -> not taken
   *
   */

  // Defines the size of the table (2^m entries)
  val m = 8
  val predictorTable = RegInit(VecInit(Seq.fill(1 << m)(1.U(2.W)))) // Initialize counters to 1 (not taken)

  val index = io.inputAddress(m - 1, 0) // Lower m bits for indexing
  val counter = predictorTable(index) // Counter for this specific index
  io.predictTaken := counter >= 2.U // Predict "taken" if counter >= 2

  // Update logic for the 2-bit counter with saturating logic
  when(io.updateStateControl) {
    when(io.inputWasTaken) {
      predictorTable(index) := Mux(counter === 3.U, 3.U, counter + 1.U)
    } .otherwise {
      predictorTable(index) := Mux(counter === 0.U, 0.U, counter - 1.U)
    }
  }

}