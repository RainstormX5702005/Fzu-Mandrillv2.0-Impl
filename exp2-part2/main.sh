#!/bin/bash
# Pintia 多文件编程题用自动化测试脚本

normaldir=$(pwd)/mandrill-src
abnormaldir=$(pwd)/mandrill-badsrc
# stdlexoutdir=$(pwd)/mandrill-lexerout
log_file=$(pwd)/$1
make clean
make 
cd build || (echo 0 >>"$log_file" && exit 0) # 如果编译失败，直接输出0分并退出
score=0
full_score=0
CCHK="java cn.edu.fzu.ccds.compilerprinciples.mandrill.parser.ParserMain"
echo $CCHK
for filec in $(ls $normaldir/*.mds); do
    full_score=$((full_score+1))
    fileout="PASS.txt"
    echo "Pass" >$fileout
    cp $filec data.mds
    echo "Testing ${filec}..."
    timeout 2 $CCHK <data.mds 1>data.parserout
    if [ $? -ne 0 ]; then
        echo "FAILED: Time Limit Exceeded or Runtime Error, return code: $?" >>"$log_file"
        echo ${filec%.mds} : FAILED >>"$log_file"
        continue
    fi
    pure_file_name=$(basename $filec)
    pure_file_name=${pure_file_name%.mds}
    diff data.parserout "$fileout" >parserout.diff.txt
    if [ ! -s parserout.diff.txt ]; then
        echo PASSED
        score=$((score+1))
        echo ${filec%.mds} : PASSED >>"$log_file"
    else
        cat parserout.diff.txt >>"$log_file"
        echo ${filec%.mds} : FAILED >>"$log_file"
    fi
    sleep 1
done

for filec in $(ls $abnormaldir/*.mds); do
    full_score=$((full_score+1))
    fileout="ERROR.txt"
    echo "Error" >$fileout
    cp $filec data.mds
    echo "Testing ${filec}..."
    timeout 2 $CCHK <data.mds 1>data.parserout
    if [ $? -ne 0 ]; then
        echo "FAILED: Time Limit Exceeded or Runtime Error, return code: $?" >>"$log_file"
        echo ${filec%.mds} : FAILED >>"$log_file"
        continue
    fi
    pure_file_name=$(basename $filec)
    pure_file_name=${pure_file_name%.mds}
    diff data.parserout "$fileout" >parserout.diff.txt
    if [ ! -s parserout.diff.txt ]; then
        echo PASSED
        score=$((score+1))
        echo ${filec%.mds} : PASSED >>"$log_file"
    else
        cat parserout.diff.txt >>"$log_file"
        echo ${filec%.mds} : FAILED >>"$log_file"
    fi
    sleep 1
done
echo $score
echo $score >>"$log_file"
cd ..
