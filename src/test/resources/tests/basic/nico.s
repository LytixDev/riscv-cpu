main:
	lw x1, 0(x1)
	addi x1, x1, 1
	done
#regset x1, 0x04
#memset 0x0,  4
#memset 0x4,  8
#memset 0x8,  12
#memset 0xc,  16
#memset 0x10, 20
