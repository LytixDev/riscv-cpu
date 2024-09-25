main:
  addi	x1, zero, 1
  addi	x2, zero, 1
  beq	x1,x1,.L2
.L1:
  addi	x1, zero, 2
.L2:
  addi	x1, zero, 3
  done
