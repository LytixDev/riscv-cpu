package barriers

import FiveStage.{ControlSignals, Instruction}
import chisel3._

class IDEX extends Module {
  val io = IO(
    new Bundle {
      val instructionIn = Input(new Instruction)
      val PCIn = Input(UInt(32.W))
      val PCNextIn = Input(UInt(32.W))
      val dataAIn = Input(UInt(32.W))
      val dataBIn = Input(UInt(32.W))
      val immIn = Input(UInt(32.W))
      val controlSignalsIn = Input(new ControlSignals)
      val ALUopIn = Input(UInt(4.W))
      val branchTypeIn = Input(UInt(3.W))
      val op1SelectIn = Input(UInt(1.W))
      val op2SelectIn = Input(UInt(1.W))
      val invalidatedIn = Input(Bool())
      val freeze = Input(Bool())

      val instructionOut = Output(new Instruction)
      val PCOut = Output(UInt(32.W))
      val PCNextOut = Output(UInt(32.W))
      val dataAOut = Output(UInt(32.W))
      val dataBOut = Output(UInt(32.W))
      val immOut = Output(UInt(32.W))
      val controlSignalsOut = Output(new ControlSignals)
      val ALUopOut = Output(UInt(4.W))
      val branchTypeOut = Output(UInt(3.W))
      val op1SelectOut = Output(UInt(1.W))
      val op2SelectOut = Output(UInt(1.W))
      val invalidatedOut = Output(Bool())
    }
  )

  val instructionReg = RegInit(0.U.asTypeOf(new Instruction))
  val PCReg = RegInit(0.U(32.W))
  val PCNextReg = RegInit(0.U(32.W))
  val dataAReg = RegInit(0.U(32.W))
  val dataBReg = RegInit(0.U(32.W))
  val immReg = RegInit(0.U(32.W))
  val controlSignalsReg = RegInit(0.U.asTypeOf(new ControlSignals))
  val ALUopReg = RegInit(0.U(4.W))
  val branchTypeReg = RegInit(0.U(3.W))
  val op1SelectReg = RegInit(0.U(1.W))
  val op2SelectReg = RegInit(0.U(1.W))
  val invalidatedReg = RegInit(false.B)

  instructionReg := Mux(io.freeze, instructionReg, io.instructionIn)
  PCReg := Mux(io.freeze, PCReg, io.PCIn)
  PCNextReg := Mux(io.freeze, PCNextReg, io.PCNextIn)
  dataAReg := Mux(io.freeze, dataAReg, io.dataAIn)
  dataBReg := Mux(io.freeze, dataBReg, io.dataBIn)
  immReg := Mux(io.freeze, immReg, io.immIn)
  controlSignalsReg := Mux(io.freeze, controlSignalsReg, io.controlSignalsIn)
  ALUopReg := Mux(io.freeze, ALUopReg, io.ALUopIn)
  branchTypeReg := Mux(io.freeze, branchTypeReg, io.branchTypeIn)
  op1SelectReg := Mux(io.freeze, op1SelectReg, io.op1SelectIn)
  op2SelectReg := Mux(io.freeze, op2SelectReg, io.op2SelectIn)
  invalidatedReg := Mux(io.freeze, invalidatedReg, io.invalidatedIn)

  io.instructionOut := instructionReg
  io.PCOut := PCReg
  io.PCNextOut := PCNextReg
  io.dataAOut := dataAReg
  io.dataBOut := dataBReg
  io.immOut := immReg
  io.controlSignalsOut := controlSignalsReg
  io.ALUopOut := ALUopReg
  io.branchTypeOut := branchTypeReg
  io.op1SelectOut := op1SelectReg
  io.op2SelectOut := op2SelectReg
  io.invalidatedOut := invalidatedReg
}