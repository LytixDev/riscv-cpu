package barriers

import FiveStage.Instruction
import chisel3._

class IFID extends Module {

  val io = IO(
    new Bundle{
      val PCIn = Input(UInt(32.W))
      val instructionIn = Input(new Instruction)

      val PCOut = Output(UInt(32.W))
      // The next PC after this one.
      // Use to validate if a speculatively taken branch was correct or not.
      val PCNextOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
    }
  )

  val PC = RegInit(0.U(32.W))
  PC := io.PCIn
  io.PCOut := PC
  io.PCNextOut := io.PCIn

  // We don't want to delay the instruction as it already takes on cycle to fetch it from the memory
  io.instructionOut := io.instructionIn
}
