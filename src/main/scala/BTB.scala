package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule


class BTBEntry extends Bundle {
  val valid = Bool()
  val tag = UInt(24.W) // The upper 24 bits of the PC
  val targetAddress = UInt(32.W)  // The target address
}

class BTB() extends MultiIOModule {

  val io = IO(
    new Bundle{
      val inputAddressIn = Input(UInt(32.W))
      val storeAddressIn = Input(UInt(32.W))
      /*
       * If true then BTB will be used for lookup.
       * If false then BTB will be used for updating the target at PC
       */
      val doLookup = Input(Bool())

      val targetAddress = Output(UInt(32.W))
      val hit = Output(Bool())
    }
  )

  /*
   * Direct mapped cache.
   * Lower 8 bits of the PC for index, and upper 26 bits of the PC for tag.
   * This means we have 2^8 = 256 entries.
   */

  val numEntries = 256
  val btb = Mem(numEntries, new BTBEntry)

  val index = io.inputAddressIn(7, 0) // Lower 8 bits for indexing
  val tag = io.inputAddressIn(31, 8) // Upper 24 bits for tag comparison

  io.targetAddress := 0.U
  io.hit := false.B

  /* Init all entries to be invalid */
  val initialising = RegInit(true.B)
  when(initialising) {
    for (i <- 0 until numEntries) {
      btb(i).valid := false.B
    }
    initialising := false.B
  }

  /* Lookup */
  when(io.doLookup && !initialising) {
    val entry = btb(index) // Fetch the entry at the index
    when (entry.valid && entry.tag === tag) {
      io.targetAddress := entry.targetAddress
      io.hit := true.B
    }
  } .elsewhen (!io.doLookup && !initialising) {
    /* Update mode: Write new target address to BTB entry */
    val entry = Wire(new BTBEntry)
    entry.valid := true.B
    entry.tag := tag
    entry.targetAddress := io.storeAddressIn
    btb.write(index, entry)
  }
}