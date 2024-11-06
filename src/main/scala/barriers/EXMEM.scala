package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

class EXMEM extends Module {

  val io = IO(
    new Bundle{
      val PCIn = Input(UInt())
      val instructionIn = Input(new Instruction)
      val dataBIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val dataAluIn = Input(UInt(32.W))
      val branchMispredictIn = Input(Bool())
      val branchtakenIn = Input(Bool())
      val invalidatedIn = Input(Bool())

      val PCOut = Output(UInt())
      val instructionOut = Output(new Instruction)
      val dataBOut = Output(UInt(32.W))
      val dataAluOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val branchMispredictOut = Output(Bool())
      val branchtakenOut = Output(Bool())
      val invalidatedOut = Output(Bool())
    }
  )

  io.PCOut := io.PCIn
  io.dataBOut := io.dataBIn
  io.dataAluOut := io.dataAluIn
  io.instructionOut := io.instructionIn
  io.controlSignalsOut := io.controlSignalsIn
  io.branchMispredictOut := io.branchMispredictIn
  io.branchtakenOut := io.branchtakenIn
  io.invalidatedOut := io.invalidatedIn
}
