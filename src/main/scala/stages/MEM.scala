package stages

import FiveStage.{ControlSignals, DMEM, DMEMsetupSignals, Instruction, MemUpdates}
import chisel3._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {
      val dataIn = Input(UInt(32.W))
      val dataAddress = Input(UInt(32.W))
      val writeEnable = Input(UInt(32.W))

      val dataOut = Output(UInt(32.W))
    })


  val DMEM = Module(new DMEM)

  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  /**
    * Your code here.
    */
  DMEM.io.dataIn      := io.dataIn
  DMEM.io.dataAddress := io.dataAddress
  DMEM.io.writeEnable := io.writeEnable

  io.dataOut := DMEM.io.dataOut
}
