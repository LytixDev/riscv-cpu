package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

class WBEND extends Module {

  val io = IO(
    new Bundle {
      val registerIn = Input(UInt(5.W))
      val dataUnwrittenIn = Input(UInt(32.W))
      val invalidatedIn = Input(Bool())
      val freeze = Input(Bool())

      val registerOut = Output(UInt(5.W))
      val dataUnwrittenOut = Output(UInt(32.W))
      val invalidatedOut = Output(Bool())
    }
  )
  val registerReg = RegInit(0.U(5.W))
  val dataUnwrittenReg = RegInit(0.U(32.W))
  val invalidatedReg = RegInit(false.B)
  val prevWasFreeze = RegInit(false.B)
  prevWasFreeze := false.B
  when (io.freeze) {
    prevWasFreeze := true.B
  }

  // TODO: this fixes one issue, but creates anothert
  when (!io.freeze) {
    registerReg := io.registerIn
    dataUnwrittenReg := io.dataUnwrittenIn
    invalidatedReg := io.invalidatedIn
  }

  io.registerOut := registerReg
  io.dataUnwrittenOut := dataUnwrittenReg
  io.invalidatedOut := invalidatedReg

}
