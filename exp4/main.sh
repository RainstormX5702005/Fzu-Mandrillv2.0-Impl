#!/bin/bash
# Pintia 多文件编程题用自动化测试脚本

normaldir=$(pwd)/mandrill-src
inputdir=$(pwd)/mandrill-in
ansdir=$(pwd)/mandrill-ans
log_file=$(pwd)/$1
make clean
make 
cd build || (echo 0 >>"$log_file" && exit 0) # 如果编译失败，直接输出0分并退出
score=0
full_score=0
MDC="java -cp .:../antlr-4.13.2-complete.jar cn.edu.fzu.ccds.compilerprinciples.mandrill.compiler.CompilerMain"
MDS="java -cp .:../antlr-4.13.2-complete.jar cn.edu.fzu.ccds.compilerprinciples.mandrill.simulator.SimulatorMain"
echo $CCHK
for filec in $(ls $normaldir/*.mds); do
    full_score=$((full_score+1))
    pure_file_name=$(basename $filec)
    pure_file_name=${pure_file_name%.mds}
    fileans=$ansdir/$pure_file_name.ans
    filein=$inputdir/$pure_file_name.in
    cp $filec data.mds
    cp $filein mandrill.in
    echo "Testing ${filec}..."
    timeout 2 $MDC data.mds data.asm
    if [ $? -ne 0 ]; then
        echo "FAILED: Time Limit Exceeded or Runtime Error, return code: $?" >>"$log_file"
        echo ${filec%.mds} : FAILED >>"$log_file"
        continue
    fi
    echo "[RUNNING] timeout $timeo $MDS data.asm mandrill.in data.out"
    timeout 2 $MDS data.asm mandrill.in data.out
    diff data.out "$fileans" >ans.diff.txt
    if [ ! -s ans.diff.txt ]; then
        echo PASSED
        score=$((score+1))
        echo ${filec%.mds} : PASSED >>"$log_file"
    else
        cat ans.diff.txt >>"$log_file"
        echo ${filec%.mds} : FAILED >>"$log_file"
    fi
    sleep 0.2
done

echo $score
echo $score >>"$log_file"
cd ..
