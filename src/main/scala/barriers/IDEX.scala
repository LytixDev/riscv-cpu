package barriers

import FiveStage.{ControlSignals, Instruction}
import chisel3._

class IDEX extends Module {
  val io = IO(
    new Bundle{
      val instructionIn = Input(new Instruction)
      val PCIn = Input(UInt())
      val dataAIn = Input(UInt(32.W))
      val dataBIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val immTypeIn = Input(UInt(3.W))
      val ALUopIn = Input(UInt(4.W))
      val branchTypeIn = Input(UInt(3.W))

      val instructionOut = Output(new Instruction)
      val PCOut = Output(UInt())
      val dataAOut = Output(UInt(32.W))
      val dataBOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val immTypeOut = Output(UInt(3.W))
      val ALUopOut = Output(UInt(4.W))
      val branchTypeOut = Output(UInt(3.W))
    }
  )

  io.instructionOut := io.instructionIn
  io.PCOut := io.PCIn
  io.dataAOut := io.dataAIn
  io.dataBOut := io.dataBIn
  io.controlSignalsOut := io.controlSignalsIn
  io.immTypeOut := io.immTypeIn
  io.ALUopOut := io.ALUopIn
  io.branchTypeOut := io.branchTypeIn
}
