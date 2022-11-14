.class public fieldTest
.super u


.field private x I
.field private y I


.method public <init>()V
	aload_0
	invokespecial u/<init>()V
	aload_0
	ldc 0
	putfield fieldTest/x I
	aload_0
	ldc 0
	putfield fieldTest/y I
	return
.end method


.method public test(ILjava/lang/String;)Ljava/lang/String;
	.limit stack 50
	.limit locals 50


	ldc 0
	iload 3
	aload_0
	getfield fieldTest/x I
	aload_0
	getfield fieldTest/y I
	iadd
	iload 1
	iadd
	istore 3
	aload_0
	iload 1
	putfield fieldTest/x I
	ldc ""
	areturn
.end method
