main:
    addi x1, zero, 8
	addi x2, zero, 4
	lw	x2,0(x1)
	sw	x2,0(x1)
    nop
    done

#memset 0x0,    0xA
#memset 0x4,    0xB
#memset 0x8, 0xC
