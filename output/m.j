.class public m
.super object


.field private y I
.field private s Ljava/lang/String;


.method public <init>()V
	aload_0
	invokespecial object/<init>()V
	aload_0
	ldc 0
	putfield m/y I
	aload_0
	ldc ""
	putfield m/s Ljava/lang/String;
	return
.end method


.method public test()I
	.limit stack 50
	.limit locals 50


	ldc 0
	iload 2
	ldc 0
	iload 3
	new arrayTest
	dup
	invokespecial arrayTest/<init>()V
	astore 1
	aload 0
	invokevirtual m/test()I
	istore 2
	aload 1
	iload 2
	iload 2
	invokevirtual arrayTest/test(II)I
	istore 3
	ldc 0
	ireturn
.end method
