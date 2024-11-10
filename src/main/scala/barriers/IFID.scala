package barriers

import FiveStage.Instruction
import chisel3._

class IFID extends Module {

  val io = IO(
    new Bundle {
      val PCIn = Input(UInt(32.W))
      val instructionIn = Input(new Instruction)
      val invalidatedIn = Input(Bool())
      val freeze = Input(Bool()) // New input for freeze signal

      val PCOut = Output(UInt(32.W))
      // The next PC after this one.
      // Used to validate if a speculatively taken branch was correct or not.
      val PCNextOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val invalidatedOut = Output(Bool())
    }
  )
  val PC = RegInit(0.U(32.W))
  PC := Mux(io.freeze, PC, io.PCIn)
  io.PCOut := PC

  val invalidated = RegInit(false.B)
  invalidated := Mux(io.freeze, invalidated, io.invalidatedIn)
  io.invalidatedOut := invalidated

  io.PCNextOut := io.PCIn

  val prevIssuedFreeze = RegInit(false.B)
  prevIssuedFreeze := false.B
  when (io.freeze) {
    prevIssuedFreeze := true.B
  }

  // We don't want to delay the instruction as it already takes one cycle to fetch it from the memory
  val prevInstruction = RegNext(io.instructionIn)
  io.instructionOut := io.instructionIn
  when (prevIssuedFreeze) {
    io.instructionOut := prevInstruction
  }
}