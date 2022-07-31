import asyncio
from util import async_timed

@async_timed()
async def cpu_bound_work() -> int:
    counter = 0
    for i in range(100000000):
        counter = counter + 1
    return counter

@async_timed()
async def main():
    task_list = []
    task_list.append(asyncio.create_task(cpu_bound_work()))
    task_list.append(asyncio.create_task(cpu_bound_work()))

    for task in task_list:
        await task

asyncio.run(main())
