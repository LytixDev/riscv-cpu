package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

class EXMEM extends Module {

  val io = IO(
    new Bundle{
      val instructionIn = Input(new Instruction)
      val dataIn = Input(UInt(32.W))
      val dataAIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)

      val instructionOut = Output(new Instruction)
      val dataOut = Output(UInt(32.W))
      val dataAOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
    }
  )

  io.dataOut := io.dataIn
  io.dataAOut := io.dataAIn

  io.instructionOut := io.instructionIn
  io.controlSignalsOut := io.controlSignalsIn
}
