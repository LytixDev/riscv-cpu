package barriers
import FiveStage.{ControlSignals, Instruction}
import chisel3._

/*
  EX.io.exmemRegister := EXMEM.instructionOut.registerRd
  EX.io.exmemInvalidated := EXMEM.invalidatedOut
  EX.io.exmemUnwritten := EXMEM.dataAluOut
  // NOTE: Store instructions use registerRd to hold the memory address
  when (EXMEM.controlSignalsOut.memWrite) {
    EX.io.exmemRegister := 0.U // Zero register forwards are ignored
 */

class WBEND extends Module {

  val io = IO(
    new Bundle{
      val registerIn = Input(UInt(5.W))
      val dataUnwrittenIn = Input(UInt(32.W))
      val invalidatedIn = Input(Bool())

      val registerOut = Output(UInt(5.W))
      val dataUnwrittenOut = Output(UInt(32.W))
      val invalidatedOut = Output(Bool())
    }
  )

  io.registerOut := RegNext(io.registerIn)
  io.dataUnwrittenOut := RegNext(io.dataUnwrittenIn)
  io.invalidatedOut := RegNext(io.invalidatedIn)
}
