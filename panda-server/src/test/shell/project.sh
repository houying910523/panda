#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # 
# @file  : project.sh
# @author: houying
# @date  : 18-1-11
# # # # # # # # # # # # # # # # # # # # # #

#登录
curl -i 'http://127.0.0.1:9090/api/login' -d 'username=ying.hou&password=123456'

# 获取project列表
curl -X GET -H "Cookie: login_user=ying.hou" "http://127.0.0.1:9090/project/list?page=1&size=20"

# 创建一个project
curl -X POST -H "Cookie: login_user=ying.hou" -H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" -d "name=test&managers=ying.hou,xiaobin.yan&retryCount=5" \
  "http://127.0.0.1:9090/project/create"
# 返回：{"status":0,"data":true,"message":null}

# 上传一个zip包
curl -P POST -H "Cookie: login_user=ying.hou" -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundarykAkVJ6w61YFcmxmx" \
    -F "file=@test.zip" \
    -F '------WebKitFormBoundarykAkVJ6w61YFcmxmx
Content-Disposition: form-data; name="file"; filename="test.zip"
Content-Type: application/x-zip-compressed
------WebKitFormBoundarykAkVJ6w61YFcmxmx--' \
    "http://127.0.0.1:9090/project/upload/4"

# 更新project信息
curl -P POST -H "Cookie: login_user=ying.hou" -H "Content-Type: application/json; charset=UTF-8" -d '{"id": 4, "name": "test2", "managers": "xiaobin.yan", "retryCount": 4}' \
  "http://127.0.0.1:9090/project/update"
