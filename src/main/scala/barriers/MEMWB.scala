package barriers

import FiveStage.{ControlSignals, Instruction}
import chisel3._

class MEMWB extends Module {

  val io = IO(
    new Bundle{
      val instructionIn = Input(new Instruction)
      val dataAluIn = Input(UInt(32.W))
      val memReadIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val branchMispredictIn = Input(Bool())
      val invalidatedIn = Input(Bool())

      val dataAluOut = Output(UInt(32.W))
      val memReadOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
      val branchMispredictOut = Output(Bool())
      val invalidatedOut = Output(Bool())
    }
  )

  // Reading latency is one cycle already
  io.memReadOut := io.memReadIn

  io.instructionOut := RegNext(io.instructionIn)
  io.dataAluOut := RegNext(io.dataAluIn)
  io.controlSignalsOut := RegNext(io.controlSignalsIn)
  io.branchMispredictOut := RegNext(io.branchMispredictIn)
  io.invalidatedOut := RegNext(io.invalidatedIn)
}