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
      val branchTakenOrJumpIn = Input(Bool())
      val invalidatedIn = Input(Bool())

      val dataAluOut = Output(UInt(32.W))
      val memReadOut = Output(UInt(32.W))
      val instructionOut = Output(new Instruction)
      val controlSignalsOut = Output(new ControlSignals)
      val branchTakenOrJumpOut = Output(Bool())
      val invalidatedOut = Output(Bool())
    }
  )

  val dataAlu = RegInit(0.U(32.W))
  dataAlu := io.dataAluIn
  val instruction = Reg(new Instruction)
  instruction := io.instructionIn
  val controlSignals = Reg(new ControlSignals)
  controlSignals := io.controlSignalsIn
  val branchTakenOrJump = Reg(Bool())
  branchTakenOrJump := io.branchTakenOrJumpIn
  val invalidated = Reg(Bool())
  invalidated := io.invalidatedIn

  // What we read from MEM. Everything else must be delayed since this takes one cycle.
  io.memReadOut := io.memReadIn

  io.dataAluOut := dataAlu
  io.instructionOut := instruction
  io.controlSignalsOut := controlSignals
  io.branchTakenOrJumpOut := branchTakenOrJump
  io.invalidatedOut := invalidated

  // NOTE: Attempt at an optimization: Only stall when we actually performed a memory read
  //       This used to work with NOP's on, but results in a combinatorial circuit with them turned off.
  // when (controlSignals.memRead || io.controlSignalsIn.memRead) {
  //   io.dataAluOut := dataAlu
  //   io.instructionOut := instruction
  //   io.controlSignalsOut := controlSignals
  // } .otherwise {
  //   io.dataAluOut := io.dataAluIn
  //   io.instructionOut := io.instructionIn
  //   io.controlSignalsOut := io.controlSignalsIn
  // }
}