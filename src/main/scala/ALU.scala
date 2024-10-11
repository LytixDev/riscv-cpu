package FiveStage
import Chisel.{Cat, Fill, MuxLookup}
import FiveStage.ALUOps.{SLT, _}
import FiveStage.ImmFormat.{BTYPE, ITYPE, JTYPE, STYPE, UTYPE}
import chisel3._
import chisel3.experimental.MultiIOModule


class ALU() extends MultiIOModule {

  val io = IO(
    new Bundle{
      val op1 = Input(UInt(32.W))
      val op2 = Input(UInt(32.W))
      val aluOp = Input(UInt(4.W))

      val aluResult = Output(UInt(32.W))
    }
  )

  val ALUopMap = Array(
    ADD    -> (io.op1 + io.op2),
    SUB    -> (io.op1 - io.op2),
    AND    -> (io.op1 & io.op2),
    OR     -> (io.op1 | io.op2),
    XOR    -> (io.op1 ^ io.op2),
    SLT    -> (io.op1.asSInt < io.op2.asSInt()).asUInt(), // Set Less Than
    SLL    -> (io.op1 << io.op2(4,0)), // Shift Left Logical (lower 5 bits of op2)
    SLTU   -> (io.op1 < io.op2), // Set Less Than Unsigned
    SRL    -> (io.op1 >> io.op2(4,0)), // Shift Right Logical (lower 5 bits of op2)
    SRA    -> (io.op1.asSInt >> io.op2(4,0)).asUInt(), // Shift Right Arithmetic (lower 5 bits of op2)
    COPY_A -> io.op1,
    COPY_B -> io.op2,
  )

  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUopMap)
}
