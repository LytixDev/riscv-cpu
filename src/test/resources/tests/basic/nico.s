main:
    addi x1, zero, 1
	lw	x2,4(x5)
	lw	x3,8(x5)
	add	x3,x2,x3
    done
	nop
	nop


#regset x5, 0

#memset 0x4, 0xA
#memset 0x8, 0xB
