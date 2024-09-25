package stages

import FiveStage._
import chisel3._
import chisel3.experimental.MultiIOModule


class Execute extends MultiIOModule {
  val io = IO(
    new Bundle{
      val instruction = Input(new Instruction)
      val PC = Input(UInt(32.W))
      val dataA = Input(UInt(32.W))
      val dataB = Input(UInt(32.W))
      val controlSignals = Input(new ControlSignals)
      val immType = Input(UInt(3.W))
      val ALUop = Input(UInt(4.W))
      val branchType = Input(UInt(3.W))

      val instructionOut = Output(new Instruction)
      val PCOut = Output(UInt(32.W))
      val aluResult = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val branchTaken = Output(Bool())
    }
  )

  io.branchTaken := false.B

  val branchCmp = Module(new BranchCmp)
  branchCmp.io.op1 := io.dataA
  branchCmp.io.op2 := io.dataB
  branchCmp.io.branchType := io.branchType
  when (io.controlSignals.branch) {
      printf("TAKEN??? op1: %d op2: %d res: %d\n", io.dataA, io.dataB, branchCmp.io.branchTaken)
     io.branchTaken := branchCmp.io.branchTaken
  }

  val alu = Module(new ALU)
  alu.io.op1 := io.dataA
  alu.io.op2 := io.dataB
  alu.io.immType := io.immType
  alu.io.aluOp := io.ALUop

  io.instructionOut := io.instruction
  io.PCOut := io.PC
  io.aluResult := alu.io.aluResult
  io.controlSignalsOut := io.controlSignals
}
