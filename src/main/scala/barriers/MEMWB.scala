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

  io.memReadOut := io.memReadIn


  val instruction = Reg(new Instruction)
  instruction := io.instructionIn
  val controlSignals = Reg(new ControlSignals)
  controlSignals := io.controlSignalsIn

  io.instructionOut := instruction
  io.controlSignalsOut := controlSignals
}
