package FiveStage
import Chisel.MuxLookup
import FiveStage.branchType._
import chisel3._
import chisel3.experimental.MultiIOModule

// Implements functionality similar to the ALU, but tailored made for the different branch types
class BranchCmp() extends MultiIOModule {
  val io = IO(
    new Bundle{
      val op1 = Input(UInt(32.W))
      val op2 = Input(UInt(32.W))
      val branchType = Input(UInt(3.W))

      val branchTaken = Output(Bool())
    }
  )

  val BranchCmpOpMap = Array(
    beq  -> (io.op1 === io.op2),
    neq  -> (io.op1 =/= io.op2),
    gte  -> (io.op1.asSInt() >= io.op2.asSInt()),
    lt   -> (io.op1.asSInt() < io.op2.asSInt()),
    gteu -> (io.op1 > io.op2),
    ltu  -> (io.op1 < io.op2)
    // Ignore DC and jump since these instructions will never enter here
  )

  io.branchTaken := MuxLookup(io.branchType, 0.U(1.W), BranchCmpOpMap)
}