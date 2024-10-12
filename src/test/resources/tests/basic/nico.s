main:
	addi x1, zero, 69
	sw x1, 0(x1)
	lw x1, 0(x1)
	done
#memset 0x0,  7
#memset 0x4,  9