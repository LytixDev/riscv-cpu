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

      val dataAluOut = Output(UInt(32.W))
      val memReadOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
    }
  )

  val dataAlu = RegInit(0.U(32.W))
  dataAlu := io.dataAluIn
  val instruction = Reg(new Instruction)
  instruction := io.instructionIn
  val controlSignals = Reg(new ControlSignals)
  controlSignals := io.controlSignalsIn

  // What we read from MEM. Everything else must be delayed since this takes one cycle.
  io.memReadOut := io.memReadIn

  // NOTE: Attempt at an optimization: Only stall when we actually performed a memory read
  when (controlSignals.memRead || io.controlSignalsIn.memRead) {
    io.dataAluOut := dataAlu
    io.instructionOut := instruction
    io.controlSignalsOut := controlSignals
  } .otherwise {
    io.dataAluOut := io.dataAluIn
    io.instructionOut := io.instructionIn
    io.controlSignalsOut := io.controlSignalsIn
  }
}