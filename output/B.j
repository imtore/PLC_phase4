.class public B
.super object




.method public <init>()V
	aload_0
	invokespecial object/<init>()V
	return
.end method


.method public test1()I
	.limit stack 50
	.limit locals 50


	ldc 0
	iload 1
	ldc 0
	iload 2
	iconst_0
	iload 5
	iconst_0
	iload 6
	iconst_0
	iload 7
	iconst_0
	iload 8
	ldc 100
	newarray int
	astore 4
	ldc 10
	newarray int
	astore 3
	ldc 1
	istore 1
	ldc 2
	istore 2
	aload 3
	aload 4
	if_acmpne Label0
	iconst_1
	goto Label1
	Label0:
	iconst_0
	Label1:
	istore 7
	iload 1
	iload 2
	if_icmpge Label2
	iconst_1
	goto Label3
	Label2:
	iconst_0
	Label3:
	istore 5
	iload 1
	iload 2
	if_icmpne Label4
	iconst_1
	goto Label5
	Label4:
	iconst_0
	Label5:
	istore 6
	iload 5
	ifeq Label6
	iconst_1
	goto Label7
	Label6:
	iload 7
	Label7:
	ifne Label8
	iconst_1
	goto Label9
	Label8:
	iconst_0
	Label9:
	istore 8
	iload 2
	ineg
	istore 1
	ldc 0
	ireturn
.end method
