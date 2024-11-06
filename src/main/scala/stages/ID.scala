package stages

import Chisel.{Cat, Fill, MuxLookup}
import FiveStage._
import chisel3._
import chisel3.experimental.MultiIOModule
import FiveStage.ImmFormat.{BTYPE, ITYPE, JTYPE, STYPE, UTYPE}


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      /**
        * TODO: Your code here.
        */
      val instruction = Input(new Instruction)
      // From the WB stage
      val writeEnable = Input(Bool())
      val writeAddress = Input(UInt(5.W))
      val writeData = Input(UInt(32.W))

      val dataA = Output(UInt(32.W))
      val dataB = Output(UInt(32.W))
      val imm = Output(UInt(32.W))
      // Directly forwarded from the Decoder
      val controlSignals = Output(new ControlSignals)
      val ALUop = Output(UInt(4.W))
      val branchType = Output(UInt(3.W))
      val op1Select = Output(UInt(1.W))
      val op2Select = Output(UInt(1.W))
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io

  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */
  registers.io.readAddress1 := io.instruction.registerRs1
  registers.io.readAddress2 := io.instruction.registerRs2
  registers.io.writeEnable  := io.writeEnable
  registers.io.writeAddress := io.writeAddress
  registers.io.writeData    := io.writeData

  decoder.instruction := io.instruction
  io.controlSignals := decoder.controlSignals
  io.branchType := decoder.branchType
  io.ALUop := decoder.ALUop
  io.op1Select := decoder.op1Select
  io.op2Select := decoder.op2Select

  // Decode and sign-extend immediate to 32-bit wide uint
  io.imm := MuxLookup(decoder.immType, 0.S(32.W), Array(
    ImmFormat.ITYPE -> io.instruction.immediateIType,
    ImmFormat.STYPE -> io.instruction.immediateSType,
    ImmFormat.JTYPE -> io.instruction.immediateJType,
    ImmFormat.BTYPE -> io.instruction.immediateBType,
    ImmFormat.UTYPE -> io.instruction.immediateUType,
    ImmFormat.SHAMT -> io.instruction.immediateZType,
    ImmFormat.DC -> 0.S
  )).asUInt

  io.dataA := registers.io.readData1
  io.dataB := registers.io.readData2
}