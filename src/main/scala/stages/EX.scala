package stages

import FiveStage._
import chisel3._
import chisel3.experimental.MultiIOModule


class Execute extends MultiIOModule {
  val io = IO(
    new Bundle{
      val PC = Input(UInt(32.W))
      val dataA = Input(UInt(32.W))
      val dataB = Input(UInt(32.W))
      val imm = Input(UInt(32.W))
      val controlSignals = Input(new ControlSignals)
      val ALUop = Input(UInt(4.W))
      val branchType = Input(UInt(3.W))
      val op1Select = Input(UInt(1.W))
      val op2Select = Input(UInt(1.W))

      val aluResult = Output(UInt(32.W))
      val branchTaken = Output(Bool())
    }
  )

  // Branch compare module
  val branchCmp = Module(new BranchCmp)
  io.branchTaken := false.B
  branchCmp.io.op1 := io.dataA
  branchCmp.io.op2 := io.dataB
  branchCmp.io.branchType := io.branchType

  when (io.controlSignals.branch) {
    io.branchTaken := branchCmp.io.branchTaken
  }

  val alu = Module(new ALU)
  alu.io.aluOp := io.ALUop
  alu.io.op1 := io.dataA
  alu.io.op2 := io.dataB
  // For branch instructions, the ALU is used for target address calculation (PC + sext(imm)
  when (io.op1Select === Op1Select.PC || io.controlSignals.branch) {
    alu.io.op1 := io.PC
  }
  when (io.op2Select === Op2Select.imm || io.controlSignals.branch) {
    alu.io.op2 := io.imm
  }

  io.aluResult := alu.io.aluResult
}
