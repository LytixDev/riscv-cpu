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
      val immIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val ALUopIn = Input(UInt(4.W))
      val branchTypeIn = Input(UInt(3.W))
      val op1SelectIn = Input(UInt(1.W))
      val op2SelectIn = Input(UInt(1.W))

      val instructionOut = Output(new Instruction)
      val PCOut = Output(UInt())
      val dataAOut = Output(UInt(32.W))
      val dataBOut = Output(UInt(32.W))
      val immOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val ALUopOut = Output(UInt(4.W))
      val branchTypeOut = Output(UInt(3.W))
      val op1SelectOut = Output(UInt(1.W))
      val op2SelectOut = Output(UInt(1.W))
    }
  )

  io.instructionOut := io.instructionIn
  io.PCOut := io.PCIn
  io.dataAOut := io.dataAIn
  io.dataBOut := io.dataBIn
  io.immOut := io.immIn
  io.controlSignalsOut := io.controlSignalsIn
  io.ALUopOut := io.ALUopIn
  io.branchTypeOut := io.branchTypeIn
  io.op1SelectOut := io.op1SelectIn
  io.op2SelectOut := io.op2SelectIn
}
