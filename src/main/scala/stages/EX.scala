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
      // Forward from EXMEM
      val exmemRegister = Input(UInt(5.W)) // The register with unwritten data
      val exmemUnwritten = Input(UInt(32.W))
      val exmemInvalidated = Input(Bool())
      // Forward from MEMWB
      val memwbRegister = Input(UInt(5.W))
      val memwbUnwritten = Input(UInt(32.W))
      val memwbInvalidated = Input(Bool())
      // Forward from WBEND
      val wbendRegister = Input(UInt(5.W))
      val wbendUnwritten = Input(UInt(32.W))
      val wbendInvalidated = Input(Bool())

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
  io.dataBOut := dataB

  // Logic for choosing the correct forwarded values.
  // NOTE: Zero register should always be zero. Instructions that don't write will get registerRd of 0,
  //       so we must ignore these types of forwards.
  // We first check the oldest value in the pipeline. This means if we get forwarded two values for the same register
  // we always just the newest one
  when (!io.wbendInvalidated && io.instruction.registerRs1 === io.wbendRegister && io.wbendRegister > 0.U) {
    dataA := io.wbendUnwritten
  }
  when (!io.wbendInvalidated && io.instruction.registerRs2 === io.wbendRegister && io.wbendRegister > 0.U) {
    dataB := io.wbendUnwritten
  }
  when (!io.memwbInvalidated && io.instruction.registerRs1 === io.memwbRegister && io.memwbRegister > 0.U) {
    dataA := io.memwbUnwritten
  }
  when (!io.memwbInvalidated && io.instruction.registerRs2 === io.memwbRegister && io.memwbRegister > 0.U) {
    dataB := io.memwbUnwritten
  }
  when (!io.exmemInvalidated && io.instruction.registerRs1 === io.exmemRegister && io.exmemRegister > 0.U) {
    dataA := io.exmemUnwritten
  }
  when (!io.exmemInvalidated && io.instruction.registerRs2 === io.exmemRegister && io.exmemRegister > 0.U) {
    dataB := io.exmemUnwritten
  }

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
