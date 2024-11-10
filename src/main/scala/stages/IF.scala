package stages

import FiveStage.{BTB, BimodalPredictor, IMEM, IMEMsetupSignals, Instruction}
import chisel3._
import chisel3.experimental.MultiIOModule

class InstructionFetch extends MultiIOModule {

  // Don't touch
  val testHarness = IO(
    new Bundle {
      val IMEMsetup = Input(new IMEMsetupSignals)
      val PC        = Output(UInt())
    }
  )


  /**
    * The instruction is of type Bundle, which means that you must
    * use the same syntax used in the testHarness for IMEM setup signals
    * further up.
    */
  val io = IO(
    new Bundle {
      val newPC = Input(UInt(32.W)) // From the execute stage
      val useNewPCControl = Input(Bool())
      // Used to update the BTB and Predictor
      val addressThatGeneratedNewPC = Input(UInt(32.W)) // Used as input to update BTB and predictor
      val updateBTB = Input(Bool())
      val updatePredictor = Input(Bool())
      val wasTaken = Input(Bool())
      val freeze = Input(Bool())

      val PC = Output(UInt(32.W))
      val instruction = Output(new Instruction)
    })

  val IMEM = Module(new IMEM)
  val PC   = RegInit(UInt(32.W), 0.U)

  val BTB = Module(new BTB)
  BTB.io.inputAddressIn := PC
  BTB.io.doLookup := true.B
  BTB.io.storeAddressIn := 0.U // Lookup mode so doesn't matter

  val bimodalPredictor = Module(new BimodalPredictor)
  bimodalPredictor.io.inputAddress := PC
  bimodalPredictor.io.inputWasTaken := io.wasTaken
  bimodalPredictor.io.updateStateControl := false.B

  /**
    * Setup. You should not change this code
    */
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress


  io.PC := PC

  // Update BTB logic
  when (io.updateBTB) {
    // printf("Updated BTB at %d with %d\n", io.addressThatGeneratedNewPC, io.newPC)
    BTB.io.doLookup := false.B
    BTB.io.inputAddressIn := io.addressThatGeneratedNewPC
    BTB.io.storeAddressIn := io.newPC
  }

  // Update predictor logic
  when (io.updatePredictor) {
    bimodalPredictor.io.inputAddress := io.addressThatGeneratedNewPC
    bimodalPredictor.io.updateStateControl := true.B
  }

  // Figure out what instruction to fetch next
  //when (io.freeze) {
  //  PC := PC // Hold the current PC value during a freeze
  //} .elsewhen (io.useNewPCControl) {
  //  PC := io.newPC
  //} .otherwise {
  //  when (bimodalPredictor.io.predictTaken && BTB.io.hit) {
  //    PC := BTB.io.targetAddress
  //  } .otherwise {
  //    PC := PC + 4.U
  //  }
  //}

  // REMOVEME: turn branch prediction back on
  when (io.freeze) {
    PC := PC // Hold the current PC value during a freeze
  } .elsewhen (io.useNewPCControl) {
    PC := io.newPC
  } .otherwise {
    PC := PC + 4.U
  }

  IMEM.io.instructionAddress := PC

  val instruction: Instruction = Wire(new Instruction)
  instruction := IMEM.io.instruction.asTypeOf(new Instruction)

  /**
    * Setup. You should not change this code.
    */
  when(testHarness.IMEMsetup.setup) {
    PC := 0.U
    instruction := Instruction.NOP
  }

  io.instruction := instruction
}
