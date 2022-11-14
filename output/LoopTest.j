.class public LoopTest
.super object




.method public <init>()V
	aload_0
	invokespecial object/<init>()V
	return
.end method


.method public testLoop()I
	.limit stack 50
	.limit locals 50


	ldc 0
	iload 1
	ldc 0
	iload 2
	ldc 0
	iload 3
	iconst_0
	iload 4
	iconst_1
	istore 4
	ldc 12
	istore 1
	ldc 13
	istore 2
	ldc 0
	istore 3
	Label10:
	iload 4
	ifeq Label11
	iload 3
	iload 1
	iadd
	iload 2
	iadd
	istore 3
	iload 1
	iload 2
	imul
	istore 3
	goto Label10
	Label11:
	iload 1
	iload 2
	if_icmpne Label12
	iconst_1
	goto Label13
	Label12:
	iconst_0
	Label13:
	ifne Label14
	ldc 14
	iload 1
	imul
	iload 2
	idiv
	istore 3
	goto Label15
	Label14:
	iload 1
	iload 2
	iload 1
	imul
	ldc 2
	imul
	iadd
	istore 3
	ldc 12
	iload 3
	imul
	istore 3
	Label15:
	ldc 0
	ireturn
.end method
