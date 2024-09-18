package barriers

import FiveStage.{ControlSignals, Instruction}
import chisel3._

class MEMWB extends Module {

  val io = IO(
    new Bundle{
      val instructionIn = Input(new Instruction)
      val dataIn = Input(UInt(32.W))
      val memReadIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)

      val dataOut = Output(UInt(32.W))
      val memReadOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
    }
  )

  val data = RegInit(0.U(32.W))
  data := io.dataIn
  io.dataOut := data

  val instruction = Reg(new Instruction)
  instruction := io.instructionIn
  io.instructionOut := instruction

  val controlSignals = Reg(new ControlSignals)
  controlSignals := io.controlSignalsIn
  io.controlSignalsOut := controlSignals

  // What we read from MEM. Everything else must be delayed since this takes one cycle.
  io.memReadOut := io.memReadIn
}
