; ModuleID = '/tmp/webcompile/_15391_0.bc'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

define i32 @main(i32 %argc, i8** %argv) nounwind {
  ; hhallloo
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  %3 = alloca i8**, align 8					;Tail-Test
  %i = alloca i32, align 4
  %a = alloca i32, align 4
  store i32 0, i32* %1
  store i32 %argc, i32* %2, align 4
  store i8** %argv, i8*** %3, align 8
  store i32 0, i32* %i, align 4
  store i32 1, i32* %a, align 4
  br label %4
  ; Halloqqq

; <label>:4                                       ; preds = %10, %0
  ; hawwoooqqqq
  %5 = load i32* %i, align 4
  %6 = icmp slt i32 %5, 10
  br i1 %6, label %tt, label %aa

tt:
  br label %7

; <label>:7                                       ; preds = %4
  %8 = load i32* %a, align 4
  %9 = add nsw i32 %8, 1						;Tail-Test
  store i32 %9, i32* %a, align 4
  ;Halloeeee
  br label %10

; <label>:10                                      ; preds = %7
  %11 = load i32* %i, align 4
  %12 = add nsw i32 %11, 1
  store i32 %12, i32* %i, align 4
  br label %4

aa:                                 ; preds = %4
  %13 = load i32* %a, align 4
  %14 = icmp eq i32 4, 5	;EQ-Test
  ret i32 %13
}
