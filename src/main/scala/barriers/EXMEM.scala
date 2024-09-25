package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

class EXMEM extends Module {

  val io = IO(
    new Bundle{
      val PCIn = Input(UInt())
      val instructionIn = Input(new Instruction)
      val dataAIn = Input(UInt(32.W))
      val dataAluIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)

      val PCOut = Output(UInt())
      val instructionOut = Output(new Instruction)
      val dataAOut = Output(UInt(32.W))
      val dataAluOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
    }
  )

  io.PCOut := io.PCIn
  io.dataAOut := io.dataAIn
  io.dataAluOut := io.dataAluIn
  io.instructionOut := io.instructionIn
  io.controlSignalsOut := io.controlSignalsIn
}
