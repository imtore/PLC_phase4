.class public arrayTest
.super object




.method public <init>()V
	aload_0
	invokespecial object/<init>()V
	return
.end method


.method public test(II)I
	.limit stack 50
	.limit locals 50


	ldc 0
	iload 5
	ldc 13
	istore 5
	ldc 10
	newarray int
	astore 3
	ldc 12
	newarray int
	astore 4
	iload 1
	ldc 2
	imul
	istore 1
	iload 1
	iload 2
	iadd
	istore 5
	aload 3
	ldc 2
	iload 5
	iastore
	aload 4
	ldc 0
	ldc 12
	iastore
	aload 3
	ldc 0
	aload 4
	ldc 0
	iaload
	iload 5
	ldc 15
	imul
	aload 4
	ldc 0
	iaload
	idiv
	iadd
	iastore
	getstatic java/lang/System/out Ljava/io/PrintStream;
	aload 3
	arraylength
	aload 4
	arraylength
	iadd
	invokevirtual java/io/PrintStream/println(I)V
	ldc 0
	ireturn
.end method
