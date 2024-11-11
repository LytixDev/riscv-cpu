package barriers

import FiveStage.{ControlSignals, Instruction}
import chisel3._

class MEMWB extends Module {

  val io = IO(
    new Bundle {
      val instructionIn = Input(new Instruction)
      val dataAluIn = Input(UInt(32.W))
      val memReadIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val branchMispredictIn = Input(Bool())
      val invalidatedIn = Input(Bool())
      val freeze = Input(Bool())

      val dataAluOut = Output(UInt(32.W))
      val memReadOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
      val branchMispredictOut = Output(Bool())
      val invalidatedOut = Output(Bool())
    }
  )
  val instructionReg = RegInit(0.U.asTypeOf(new Instruction))
  val dataAluReg = RegInit(0.U(32.W))
  val controlSignalsReg = RegInit(0.U.asTypeOf(new ControlSignals))
  val branchMispredictReg = RegInit(false.B)
  val invalidatedReg = RegInit(false.B)
  val memReadReg = RegInit(0.U(32.W))
  val prevWasFreeze = RegInit(false.B)
  prevWasFreeze := false.B
  when (io.freeze) {
    prevWasFreeze := true.B
  }

  instructionReg := Mux(io.freeze, instructionReg, io.instructionIn)
  dataAluReg := Mux(io.freeze, dataAluReg, io.dataAluIn)
  controlSignalsReg := Mux(io.freeze, controlSignalsReg, io.controlSignalsIn)
  branchMispredictReg := Mux(io.freeze, branchMispredictReg, io.branchMispredictIn)
  invalidatedReg := Mux(io.freeze, invalidatedReg, io.invalidatedIn)
  memReadReg := io.memReadIn

  //instructionReg := io.instructionIn
  //dataAluReg := io.dataAluIn
  //controlSignalsReg := io.controlSignalsIn
  //branchMispredictReg := io.branchMispredictIn
  //invalidatedReg := io.invalidatedIn

  // Direct output for memReadOut, as it doesn't need to be held during freeze
  io.memReadOut := io.memReadIn

  when (prevWasFreeze) {
    io.memReadOut := memReadReg
  }

  io.instructionOut := instructionReg
  io.dataAluOut := dataAluReg
  io.controlSignalsOut := controlSignalsReg
  io.branchMispredictOut := branchMispredictReg
  io.invalidatedOut := invalidatedReg
}
