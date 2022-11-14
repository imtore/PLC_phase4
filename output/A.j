.class public A
.super object


.field private x I


.method public <init>()V
	aload_0
	invokespecial object/<init>()V
	aload_0
	ldc 0
	putfield A/x I
	return
.end method


.method public test1()I
	.limit stack 50
	.limit locals 50


	iconst_0
	iload 2
	iconst_0
	iload 3
	ldc 0
	iload 4
	ldc 4
	newarray int
	astore 1
	aload 1
	ldc 0
	ldc 1200
	iastore
	aload 1
	ldc 1
	ldc 1201
	iastore
	aload 1
	ldc 3
	ldc 1
	ldc 2
	iadd
	iastore
	getstatic java/lang/System/out Ljava/io/PrintStream;
	aload 1
	invokevirtual java/io/PrintStream/println(Ljava/lang/Object;)V
	ldc 12
	ldc 2
	ldc 4
	imul
	iadd
	ldc 23
	isub
	ldc 22
	ldc 23
	imul
	iadd
	istore 4
	ldc 0
	ireturn
.end method
