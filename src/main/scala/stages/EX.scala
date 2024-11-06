package stages

import FiveStage._
import chisel3._
import chisel3.experimental.MultiIOModule


class Execute extends MultiIOModule {
  val io = IO(
    new Bundle{
      val PC = Input(UInt(32.W))
      val PCNext = Input(UInt(32.W))
      val instruction = Input(new Instruction)
      val dataA = Input(UInt(32.W))
      val dataB = Input(UInt(32.W))
      val imm = Input(UInt(32.W))
      val controlSignals = Input(new ControlSignals)
      val ALUop = Input(UInt(4.W))
      val branchType = Input(UInt(3.W))
      val op1Select = Input(UInt(1.W))
      val op2Select = Input(UInt(1.W))
      // Forward from MEMWB Barrier
      val registerRd = Input(UInt(5.W)) // The register with unwritten data
      val unwrittenData = Input(UInt(32.W)) // Unwritten data
      val forwardedInvalidated = Input(Bool())

      val aluResult = Output(UInt(32.W))
      val dataBOut = Output(UInt(32.W)) // If dataB is stale we update it here and need to propagate that
      val branchMispredict = Output(Bool())
      val branchTaken = Output(Bool())
    }
  )

  // Branch compare module
  val branchCmp = Module(new BranchCmp)

  val dataA = Wire(UInt(32.W))
  val dataB = Wire(UInt(32.W))
  dataA := io.dataA
  dataB := io.dataB
  // Forward if data is stale.
  // NOTE: Zero register should always be zero. Instructions that don't write will get registerRd of 0,
  //       so we must ignore these types of forwards.
  when (!io.forwardedInvalidated && io.instruction.registerRs1 === io.registerRd && io.registerRd > 0.U) {
    dataA := io.unwrittenData
  }
  when (!io.forwardedInvalidated && io.instruction.registerRs2 === io.registerRd && io.registerRd > 0.U) {
    dataB := io.unwrittenData
  }
  io.dataBOut := dataB

  branchCmp.io.op1 := dataA
  branchCmp.io.op2 := dataB
  branchCmp.io.branchType := io.branchType

  io.branchTaken := branchCmp.io.branchTaken || io.controlSignals.jump

  val alu = Module(new ALU)
  alu.io.aluOp := io.ALUop
  alu.io.op1 := dataA
  alu.io.op2 := dataB
  // For branch instructions, the ALU is used for target address calculation (PC + sext(imm)
  when (io.op1Select === Op1Select.PC || io.controlSignals.branch) {
    alu.io.op1 := io.PC
  }
  when (io.op2Select === Op2Select.imm || io.controlSignals.branch) {
    alu.io.op2 := io.imm
  }

  io.aluResult := alu.io.aluResult


  /*
   * Process for detecting a branch mispredict:
   *  If jump or branch is computed taken: ALU result != nextPC => mispredict
   *  If branch is computed not taken:         PC + 4 != nextPC => mispredict
   */
  io.branchMispredict := false.B
  when (io.controlSignals.jump || (io.controlSignals.branch && branchCmp.io.branchTaken)) {
    /* Branch and jumps we know SHOULD be taken */
    io.branchMispredict := alu.io.aluResult =/= io.PCNext
  } .elsewhen (io.controlSignals.branch) {
    /* Branch we now should NOT be taken */
    io.branchMispredict := (io.PC + 4.U) =/= io.PCNext
    when (io.branchMispredict) {
      io.aluResult := io.PC + 4.U // Hacky solution so we can update the PC to the correct address
    }
  }
}
