package FiveStage

import barriers.{EXMEM, IDEX, IFID, MEMWB}
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

  // IFID
  IFID.PCIn := IF.io.PC
  IFID.instructionIn := IF.io.instruction
  ID.io.PC := IFID.PCOut
  ID.io.instruction := IFID.instructionOut


  // IFEX
  IDEX.instructionIn := ID.io.instructionOut
  IDEX.PCIn := ID.io.PCOut
  IDEX.dataAIn := ID.io.dataA
  IDEX.dataBIn := ID.io.dataB
  IDEX.controlSignalsIn := ID.io.controlSignals
  IDEX.immTypeIn := ID.io.immType
  IDEX.ALUopIn := ID.io.ALUop

  EX.io.instruction := IDEX.instructionOut
  EX.io.PC := IDEX.PCOut
  EX.io.dataA := IDEX.dataAOut
  EX.io.dataB := IDEX.dataBOut
  EX.io.controlSignals := IDEX.controlSignalsOut
  EX.io.immType := IDEX.immTypeOut
  EX.io.ALUop := IDEX.ALUopOut


  // EXMEM
  EXMEM.instructionIn := EX.io.instructionOut
  EXMEM.dataAluIn := EX.io.aluResult
  EXMEM.dataAIn := EX.io.dataAOut
  EXMEM.controlSignalsIn := EX.io.controlSignalsOut

  MEM.io.instructionIn := EXMEM.instructionOut
  MEM.io.controlSignalsIn := EXMEM.controlSignalsOut

  MEM.io.dataIn := EXMEM.dataAluOut
  MEM.io.dataAddress := 0.U
  MEM.io.writeEnable := false.B


  // MEMWB
  MEMWB.instructionIn := MEM.io.instructionOut
  MEMWB.dataIn := EXMEM.dataAluOut
  MEMWB.memReadIn := MEM.io.dataOut
  MEMWB.controlSignalsIn := MEM.io.controlSignalsOut

  ID.io.writeEnable := MEMWB.controlSignalsOut.regWrite
  when (MEMWB.instructionOut.registerRd === 0.U) {
    ID.io.writeEnable := false.B
  }.otherwise {
    ID.io.writeEnable := MEMWB.controlSignalsOut.regWrite
  }

  ID.io.writeData := MEMWB.dataOut
  ID.io.writeAddress := MEMWB.instructionOut.registerRd

  // Memory read and write
  when (EXMEM.controlSignalsOut.memRead || EXMEM.controlSignalsOut.memWrite) {
    MEM.io.dataAddress := EXMEM.dataAluOut
    MEM.io.writeEnable := EXMEM.controlSignalsOut.memWrite
  }
  // Use dataA as data to write to memory
  when (EXMEM.controlSignalsOut.memWrite) {
    MEM.io.dataIn := EXMEM.dataAOut
  }
  // Use read data for writing to register
  when (MEMWB.controlSignalsOut.memRead) {
    ID.io.writeData := MEMWB.memReadOut
  }
}
