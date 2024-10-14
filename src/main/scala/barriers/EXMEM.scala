package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

class EXMEM extends Module {

  val io = IO(
    new Bundle{
      val PCIn = Input(UInt())
      val instructionIn = Input(new Instruction)
      val dataBIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val dataAluIn = Input(UInt(32.W))
      val branchTakenOrJumpIn = Input(Bool())
      val invalidatedIn = Input(Bool())

      val PCOut = Output(UInt())
      val instructionOut = Output(new Instruction)
      val dataBOut = Output(UInt(32.W))
      val dataAluOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val branchTakenOrJumpOut = Output(Bool())
      val invalidatedOut = Output(Bool())
    }
  )

  // io.PCOut := RegNext(io.PCIn, 0.U)
  // io.dataBOut := RegNext(io.dataBIn, 0.U)
  // io.dataAluOut := RegNext(io.dataAluIn, 0.U)
  // io.instructionOut := RegNext(io.instructionIn, Instruction.NOP)
  // io.controlSignalsOut := RegNext(io.controlSignalsIn, ControlSignals.nop)
  // io.branchTakenOrJumpOut := RegNext(io.branchTakenOrJumpIn, false.B)
  // io.invalidatedOut := RegNext(io.invalidatedIn, false.B)

  io.PCOut := io.PCIn
  io.dataBOut := io.dataBIn
  io.dataAluOut := io.dataAluIn
  io.instructionOut := io.instructionIn
  io.controlSignalsOut := io.controlSignalsIn
  io.branchTakenOrJumpOut := io.branchTakenOrJumpIn
  io.invalidatedOut := io.invalidatedIn
}
