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
  val IFBarrier  = Module(new IFID).io
  val IDBarrier  = Module(new IDEX).io
  val EXBarrier  = Module(new EXMEM).io
  val MEMBarrier = Module(new MEMWB).io

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
  IFBarrier.PCIn := IF.io.PC
  IFBarrier.instructionIn := IF.io.instruction
  ID.io.PC := IFBarrier.PCOut
  ID.io.instruction := IFBarrier.instructionOut


  // IFEX
  IDBarrier.instructionIn := ID.io.instructionOut
  IDBarrier.PCIn := ID.io.PCOut
  IDBarrier.dataAIn := ID.io.dataA
  IDBarrier.dataBIn := ID.io.dataB
  IDBarrier.controlSignalsIn := ID.io.controlSignals
  IDBarrier.immTypeIn := ID.io.immType
  IDBarrier.ALUopIn := ID.io.ALUop

  EX.io.instruction := IDBarrier.instructionOut
  EX.io.PC := IDBarrier.PCOut
  EX.io.dataA := IDBarrier.dataAOut
  EX.io.dataB := IDBarrier.dataBOut
  EX.io.controlSignals := IDBarrier.controlSignalsOut
  EX.io.immType := IDBarrier.immTypeOut
  EX.io.ALUop := IDBarrier.ALUopOut


  // EXMEM
  EXBarrier.instructionIn := EX.io.instructionOut
  EXBarrier.dataIn := EX.io.aluResult
  EXBarrier.dataAIn := EX.io.dataAOut
  EXBarrier.controlSignalsIn := EX.io.controlSignalsOut

  MEM.io.instructionIn := EXBarrier.instructionOut
  MEM.io.controlSignalsIn := EXBarrier.controlSignalsOut

  MEM.io.dataIn := EXBarrier.dataOut
  MEM.io.dataAddress := 0.U
  MEM.io.writeEnable := false.B


  // MEMWB
  MEMBarrier.instructionIn := MEM.io.instructionOut
  MEMBarrier.dataIn := EXBarrier.dataOut
  MEMBarrier.memReadIn := MEM.io.dataOut
  MEMBarrier.controlSignalsIn := MEM.io.controlSignalsOut

  ID.io.writeEnable := MEMBarrier.controlSignalsOut.regWrite
  when(MEMBarrier.instructionOut.registerRd === 0.U) {
    ID.io.writeEnable := false.B
  }.otherwise {
    ID.io.writeEnable := MEMBarrier.controlSignalsOut.regWrite
  }

  ID.io.writeData := MEMBarrier.dataOut
  ID.io.writeAddress := MEMBarrier.instructionOut.registerRd

  // Memory read and write
  when (EXBarrier.controlSignalsOut.memRead || EXBarrier.controlSignalsOut.memWrite) {
    MEM.io.dataAddress := EXBarrier.dataOut
    MEM.io.writeEnable := EXBarrier.controlSignalsOut.memWrite
  }

  when (EXBarrier.controlSignalsOut.memWrite) {
    MEM.io.dataIn := EXBarrier.dataAOut
  }

  when (MEMBarrier.controlSignalsOut.memRead) {
    ID.io.writeData := MEMBarrier.memReadOut
  }
}
