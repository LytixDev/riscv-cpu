main:
  addi t0, zero, 10
  addi t1, zero, 1
.L2:
  blt t0, t1, .L1
  sub t0, t0, t1
  j .L2
.L1:
  add t0, t1, t2
  done