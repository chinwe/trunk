#!/usr/bin/env python
# -*- coding: utf-8 -*-

import asyncio
import csv
import json
import logging

# pip install aiohttp
import aiohttp

API_BASE_URL = 'https://api.com'

# 输入数据定义
class InputData(object):

    def __init__(self, l):
        self.id = l[0]
        self.ak = l[1]
        self.sk = l[2]
        self.region = l[3]

# 输出数据定义
class OutputData(InputData):

    def __init__(self, input, result):
        super().__init__(l = [input.id, input.ak, input.sk, input.region])
        self.result = result

    def to_list(self):
        return [self.id, self.ak, self.sk, self.region, self.result]

# api返回定义
class ApiResponse(object):
    def __init__(self, code, msg):
        self.code = code
        self.msg = msg

    def ok(self):
        return (self.code == '200')

    def fail(self):
        return not self.ok()

    # 反序列化
    def deserialize(json):
        return ApiResponse(json["code"], json["msg"])

# 业务处理
async def biz(input_data):
    async with aiohttp.ClientSession(base_url=API_BASE_URL) as session:
        async with session.post(url='/api/lapp/token/get?appKey=' + input_data.ak + '&appSecret=' + input_data.sk) as rsp:
            rsp_body = await rsp.text()
            logging.info(rsp.status)
            logging.info(rsp_body)

            api_reponse = json.loads(rsp_body, object_hook=ApiResponse.deserialize)

            if (rsp.status != 200) or api_reponse.fail():
                raise RuntimeError(str(rsp.status) + ' ' + rsp_body)
            else:
                pass

# 任务单元
async def job(input_data):
    output_data = OutputData(input_data, '')
    # 异常捕获
    try:
        await biz(input_data)
        output_data.result = 'ok'
    except Exception as e:
        logging.warning(e)
        output_data.result = e

    return output_data
    
# 数据处理
async def process_data(input_datas):
    result = []
    jobs = []

    for data in input_datas:
        jobs.append(job(data))

    result = await asyncio.gather(*jobs)
    return result

# 从文件中加载数据
def load_data(filename):
    logging.info('load_data begin. [filename = %s]', filename)
    input_datas = []
    with open(filename, 'rt') as f:
        cr = csv.reader(f)
        # 跳过首行
        next(cr)
        for row in cr:
            input_datas.append(InputData(row))
    logging.info('load_data end.')
    return input_datas

# 结果写入csv
def output_result(result, filename):
    logging.info('output_result begin. [filename = %s]', filename)
    rl = []
    for r in result:
        rl.append(r.to_list())

    with open(filename, 'wt', encoding='utf-8', newline='') as f:
        cw = csv.writer(f)
        cw.writerows(rl)
    logging.info('output_result end.')

# 主方法
async def main():
    input_data = load_data('./data.csv')
    result = await process_data(input_data)
    output_result(result, 'result.csv')
    
# 日志配置
def init_logging_config():
    LOG_FORMAT = "[%(asctime)s] [%(levelname)s] [%(thread)s] [%(funcName)s(%(filename)s:%(lineno)s)] %(message)s"
    logging.basicConfig(filename="debug.log", level=logging.DEBUG, format=LOG_FORMAT) 
    # 输出到控制台
    logger = logging.getLogger("")
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.DEBUG)
    console_handler.setFormatter(logging.Formatter(LOG_FORMAT))
    logger.addHandler(console_handler)

if __name__ == '__main__':
    init_logging_config()
    asyncio.run(main())
