main:
 addi t0, zero, 3
 addi t1, zero, 1
 j .L1
 addi t1, zero, 2
 addi t2, zero, 2
 addi t3, zero, 2
 nop
 nop
.L1:
 add t0, t1, t2
 done

