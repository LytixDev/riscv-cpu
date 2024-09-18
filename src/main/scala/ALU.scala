package FiveStage
import Chisel.{Cat, Fill, MuxLookup}
import FiveStage.ALUOps.{SLT, _}
import FiveStage.ImmFormat.{ITYPE, STYPE}
import chisel3._
import chisel3.experimental.MultiIOModule


class ALU() extends MultiIOModule {

  val io = IO(
    new Bundle{
      val op1 = Input(UInt(32.W))
      val op2 = Input(UInt(32.W))
      val immType = Input(UInt(3.W))
      val aluOp = Input(UInt(4.W))

      val aluResult = Output(UInt(32.W))

    }
  )

  // Sign-extend op2 to 32-bit wide wide uint if it is an immediate
  val op2Final = Wire(UInt(32.W))
  op2Final := io.op2
  when (io.immType === ITYPE) {
    op2Final := Cat(Fill(32 - 12, io.op2(11)), io.op2(11, 0)).asUInt()
  }
  when (io.immType === STYPE) {
    op2Final := Cat(Fill(32 - 7, io.op2(6)), io.op2(6, 0)).asUInt()
  }

  val ALUopMap = Array(
    ADD    -> (io.op1 + op2Final),
    SUB    -> (io.op1 - op2Final),
    AND    -> (io.op1 & op2Final),
    OR     -> (io.op1 | op2Final),
    XOR    -> (io.op1 ^ op2Final),
    SLT    -> (io.op1.asSInt < op2Final.asSInt()).asUInt(), // Set Less Than
    SLL    -> (io.op1 << op2Final(4,0)), // Shift Left Logical (lower 5 bits of op2)
    SLTU   -> (io.op1 < op2Final), // Set Less Than Unsigned
    SRL    -> (io.op1 >> op2Final(4,0)), // Shift Right Logical (lower 5 bits of op2)
    SRA    -> (io.op1.asSInt >> op2Final(4,0)).asUInt(), // Shift Right Arithmetic (lower 5 bits of op2)

    DC     -> 0.U(32.W) // DC means Don't Care? Idk, but this should be a no op.
  )

  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUopMap)
}
