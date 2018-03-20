#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # 
# @file  : make_hadoop_conf.sh
# @author: houying
# @date  : 17-5-23
# # # # # # # # # # # # # # # # # # # # # #

if [[ $# -lt 2 ]]
then
    echo "usage $0 <yarn cluster master host> <output path>"
    exit 1
fi

set -e

master_host="$1"
output_path="$2"
base_dir=$(cd "$(dirname $0)";pwd)

if [[ -d ${output_path} ]]
then
    rm -rf ${output_path}
fi

cp -r ${base_dir}/hadoop-conf-template "$output_path"

cd ${output_path}

for sub_module in $(ls .)
do
    cd ${sub_module}
    for file in $(ls .)
    do
        sed -i "s/#{master_host_name}/${master_host}/g" "${file}"
    done
    cd ${output_path}
done
