#!/usr/bin/env python
# -*- coding: utf-8 -*-

import asyncio
import string

from util import delay, async_timed

@async_timed()
async def my_co(name: string) -> None:
    await delay(1)
    print(f'Hello {name}')

@async_timed()
async def main():
    delay_times = [3, 2, 3]
    tasks = [asyncio.create_task(delay(seconds)) for seconds in delay_times]
    [await task for task in tasks]

if __name__ == '__main__':
    '''
    asyncio.run(my_co('world'))
    '''
    asyncio.run(main())
