.class public MainClass
.super object




.method public <init>()V
	aload_0
	invokespecial object/<init>()V
	return
.end method


.method public static main([Ljava/lang/String;)V
	.limit stack 100
	.limit locals 10


	getstatic java/lang/System/out Ljava/io/PrintStream;
	ldc 10
	newarray int
	invokevirtual java/io/PrintStream/println(Ljava/lang/Object;)V
	return
.end method
