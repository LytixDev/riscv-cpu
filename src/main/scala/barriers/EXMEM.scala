package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

class EXMEM extends Module {

  val io = IO(
    new Bundle {
      val PCIn = Input(UInt())
      val instructionIn = Input(new Instruction)
      val dataBIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val dataAluIn = Input(UInt(32.W))
      val branchMispredictIn = Input(Bool())
      val branchtakenIn = Input(Bool())
      val invalidatedIn = Input(Bool())
      val freeze = Input(Bool()) // New input for freeze signal

      val PCOut = Output(UInt())
      val instructionOut = Output(new Instruction)
      val dataBOut = Output(UInt(32.W))
      val dataAluOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val branchMispredictOut = Output(Bool())
      val branchtakenOut = Output(Bool())
      val invalidatedOut = Output(Bool())
    }
  )

  // Register definitions with freeze logic using Mux
  val PCReg = RegInit(0.U)
  val instructionReg = RegInit(0.U.asTypeOf(new Instruction))
  val dataBReg = RegInit(0.U(32.W))
  val dataAluReg = RegInit(0.U(32.W))
  val controlSignalsReg = RegInit(0.U.asTypeOf(new ControlSignals))
  val branchMispredictReg = RegInit(false.B)
  val branchtakenReg = RegInit(false.B)
  val invalidatedReg = RegInit(false.B)

  // Update logic with freeze functionality
  PCReg := Mux(io.freeze, PCReg, io.PCIn)
  instructionReg := Mux(io.freeze, instructionReg, io.instructionIn)
  dataBReg := Mux(io.freeze, dataBReg, io.dataBIn)
  dataAluReg := Mux(io.freeze, dataAluReg, io.dataAluIn)
  controlSignalsReg := Mux(io.freeze, controlSignalsReg, io.controlSignalsIn)
  branchMispredictReg := Mux(io.freeze, branchMispredictReg, io.branchMispredictIn)
  branchtakenReg := Mux(io.freeze, branchtakenReg, io.branchtakenIn)
  invalidatedReg := Mux(io.freeze, invalidatedReg, io.invalidatedIn)

  // Outputs
  io.PCOut := PCReg
  io.instructionOut := instructionReg
  io.dataBOut := dataBReg
  io.dataAluOut := dataAluReg
  io.controlSignalsOut := controlSignalsReg
  io.branchMispredictOut := branchMispredictReg
  io.branchtakenOut := branchtakenReg
  io.invalidatedOut := invalidatedReg
}