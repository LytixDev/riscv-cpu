package FiveStage

import barriers.{EXMEM, IDEX, IFID, MEMWB, WBEND}
import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._
import stages.{Execute, InstructionDecode, InstructionFetch, MemoryFetch}


class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
  val IFID  = Module(new IFID).io
  val IDEX  = Module(new IDEX).io
  val EXMEM  = Module(new EXMEM).io
  val MEMWB = Module(new MEMWB).io
  val WBEND = Module(new WBEND).io

  val IF  = Module(new InstructionFetch)
  val ID  = Module(new InstructionDecode)
  val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  // val WB  = Module(new Execute) (You may not need this one?)

  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC


  /**
    TODO: Your code here
    */

  // Branch prediction metrics
  // val totalBranches = RegInit(0.U(32.W))
  // val totalMispredicts = RegInit(0.U(32.W))
  // when (EX.io.controlSignals.branch) {
  //   totalBranches := totalBranches + 1.U
  //   when (EX.io.branchMispredict) {
  //     totalMispredicts := totalMispredicts + 1.U
  //   }
  // }
  // printf("mispredicts: [%d] / branches: [%d]\n", totalMispredicts, totalBranches)

  // IFID Barrier
  IFID.PCIn := IF.io.PC
  IFID.instructionIn := IF.io.instruction

  // Instruction Decode Stage
  ID.io.instruction := IFID.instructionOut

  // IDEX Barrier
  IDEX.instructionIn := IFID.instructionOut
  IDEX.PCIn := IFID.PCOut
  IDEX.PCNextIn := IFID.PCNextOut
  IDEX.dataAIn := ID.io.dataA
  IDEX.dataBIn := ID.io.dataB
  IDEX.immIn := ID.io.imm
  IDEX.controlSignalsIn := ID.io.controlSignals
  IDEX.ALUopIn := ID.io.ALUop
  IDEX.branchTypeIn := ID.io.branchType
  IDEX.op1SelectIn := ID.io.op1Select
  IDEX.op2SelectIn := ID.io.op2Select

  // Execute Stage
  EX.io.op1Select := IDEX.op1SelectOut
  EX.io.op2Select := IDEX.op2SelectOut
  EX.io.PC := IDEX.PCOut
  EX.io.PCNext := IDEX.PCNextOut
  EX.io.instruction := IDEX.instructionOut
  EX.io.dataA := IDEX.dataAOut
  EX.io.dataB := IDEX.dataBOut
  EX.io.imm := IDEX.immOut
  EX.io.controlSignals := IDEX.controlSignalsOut
  EX.io.ALUop := IDEX.ALUopOut
  EX.io.branchType := IDEX.branchTypeOut
  // Freeze signals
  IF.io.freeze := EX.io.issueFreeze
  IFID.freeze := EX.io.issueFreeze
  IDEX.freeze := EX.io.issueFreeze
  EXMEM.freeze := EX.io.issueFreeze
  MEMWB.freeze := EX.io.issueFreeze
  WBEND.freeze := EX.io.issueFreeze
  // Handle branch mispredicts
  // Send signal to IFID and IDEX that speculatively fetched branch was wrong
  IFID.invalidatedIn := EX.io.branchMispredict && !IDEX.invalidatedOut
  IDEX.invalidatedIn := (EX.io.branchMispredict && !IDEX.invalidatedOut) || IFID.invalidatedOut
  // Signal to the IF to use the incoming newPC
  IF.io.useNewPCControl := EX.io.branchMispredict && !IDEX.invalidatedOut
  IF.io.newPC := EX.io.aluResult
  // We only update a BTB entry when we mispredict a branch address which is actually taken
  // When we mispredict a branch address that is not taken, it is uninteresting to fill the BTB with PC + 4.
  IF.io.updateBTB := EX.io.branchTaken && (EX.io.branchMispredict && !IDEX.invalidatedOut) //EXMEM.branchMispredictOut && EXMEM.branchtakenOut
  IF.io.addressThatGeneratedNewPC := IDEX.PCOut
  IF.io.updatePredictor := IDEX.controlSignalsOut.jump || IDEX.controlSignalsOut.branch
  IF.io.wasTaken := EX.io.branchTaken

  // EXMEM Barrier
  EXMEM.PCIn := IDEX.PCOut
  EXMEM.instructionIn := IDEX.instructionOut
  EXMEM.dataBIn := EX.io.dataBOut
  EXMEM.controlSignalsIn := IDEX.controlSignalsOut
  EXMEM.dataAluIn := EX.io.aluResult
  EXMEM.invalidatedIn := IDEX.invalidatedOut // || EX.io.branchMispredict
  EXMEM.branchMispredictIn := EX.io.branchMispredict
  EXMEM.branchtakenIn := EX.io.branchTaken && !IDEX.invalidatedOut

  // Memory Stage
  MEM.io.dataAddress := EXMEM.dataAluOut
  MEM.io.writeEnable := EXMEM.controlSignalsOut.memWrite && !EXMEM.invalidatedOut
  MEM.io.dataIn := EXMEM.dataAluOut
  when (EXMEM.controlSignalsOut.memWrite) {
    MEM.io.dataIn := EXMEM.dataBOut
  }

  // MEMWB Barrier
  // If instruction in MEMWB barrier was taken, then we need to invalidate current instruction in EXMEM barrier
  MEMWB.memReadIn := MEM.io.dataOut // What we read from memory
  MEMWB.instructionIn := EXMEM.instructionOut
  MEMWB.controlSignalsIn := EXMEM.controlSignalsOut
  MEMWB.dataAluIn := EXMEM.dataAluOut
  MEMWB.branchMispredictIn := EXMEM.branchMispredictOut
  MEMWB.invalidatedIn := EXMEM.invalidatedOut // EX.io.prevIssuedFreeze
  // For jump instructions, the alu result is used to update the new PC, while the
  // data we actually want to write to the given register is the old PC + 4.
  when (EXMEM.controlSignalsOut.jump) {
    MEMWB.dataAluIn := EXMEM.PCOut + 4.U
  }
  // Instruction Decode extra from MEMWB Barrier
  ID.io.writeAddress := MEMWB.instructionOut.registerRd
  ID.io.writeData := MEMWB.dataAluOut
  // For read instructions we write the data we read
  when (MEMWB.controlSignalsOut.memRead) {
    ID.io.writeData := MEMWB.memReadOut
  }
  // Register write
  ID.io.writeEnable := MEMWB.controlSignalsOut.regWrite && !MEMWB.invalidatedOut && !EX.io.prevIssuedFreeze

  // WBEND Barrier
  WBEND.registerIn := MEMWB.instructionOut.registerRd
  WBEND.dataUnwrittenIn := MEMWB.dataAluOut
  WBEND.invalidatedIn := MEMWB.invalidatedOut
  // NOTE: Store instructions use registerRd to hold the memory address
  when (MEMWB.controlSignalsOut.memWrite) {
    WBEND.registerIn := 0.U // Zero register forwards are ignored
  }
  when (MEMWB.controlSignalsOut.memRead) {
    WBEND.dataUnwrittenIn := MEMWB.memReadOut
  }

  // Forwards to Execute stage
  // EXMEM to EX
  EX.io.exmemRegister := EXMEM.instructionOut.registerRd
  EX.io.exmemInvalidated := EXMEM.invalidatedOut
  EX.io.exmemUnwritten := EXMEM.dataAluOut
  when (EX.io.prevIssuedFreeze) {
    EX.io.exmemUnwritten := MEM.io.dataOut
  }

  EX.io.exmemIsLoad := EXMEM.controlSignalsOut.memRead
  // NOTE: Store instructions use registerRd to hold the memory address
  when (EXMEM.controlSignalsOut.memWrite) {
    EX.io.exmemRegister := 0.U // Zero register forwards are ignored
  }
  // MEMWB to EX
  EX.io.memwbRegister := MEMWB.instructionOut.registerRd
  EX.io.memwbInvalidated := MEMWB.invalidatedOut
  EX.io.memwbUnwritten := MEMWB.dataAluOut
  // NOTE: Store instructions use registerRd to hold the memory address
  when (MEMWB.controlSignalsOut.memWrite) {
    EX.io.memwbRegister := 0.U // Zero register forwards are ignored
  }
  when (MEMWB.controlSignalsOut.memRead) {
     EX.io.memwbUnwritten := MEMWB.memReadOut
  }
  // WB to EX
  EX.io.wbendRegister := WBEND.registerOut
  EX.io.wbendInvalidated := WBEND.invalidatedOut
  EX.io.wbendUnwritten := WBEND.dataUnwrittenOut

}
