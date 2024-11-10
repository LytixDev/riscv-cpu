package barriers

import FiveStage.Instruction
import chisel3._

class IFID extends Module {

  val io = IO(
    new Bundle {
      val PCIn = Input(UInt(32.W))
      val instructionIn = Input(new Instruction)
      val freeze = Input(Bool()) // New input for freeze signal

      val PCOut = Output(UInt(32.W))
      // The next PC after this one.
      // Used to validate if a speculatively taken branch was correct or not.
      val PCNextOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
    }
  )

  val PC = RegInit(0.U(32.W))
  PC := Mux(io.freeze, PC, io.PCIn)
  val prevInstruction = RegNext(io.instructionIn)

  val prevIssuedFreeze = RegInit(false.B)
  prevIssuedFreeze := false.B
  when (io.freeze) {
    prevIssuedFreeze := true.B
  }

  io.PCOut := PC
  io.PCNextOut := io.PCIn

  // We don't want to delay the instruction as it already takes one cycle to fetch it from the memory
  io.instructionOut := io.instructionIn
  when (prevIssuedFreeze) {
    io.instructionOut := prevInstruction
  }
}