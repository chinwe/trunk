import asyncio
from concurrent.futures import ProcessPoolExecutor
from functools import partial

from util import async_timed

def count(count_to: int) -> int:
    counter = 0
    for i in range(count_to):
        counter = counter + 1
    return counter

@async_timed()
async def main():
    with ProcessPoolExecutor() as process_pool:
        loop = asyncio.get_running_loop()
        nums = [1, 3, 5, 22, 100000000]
        calls = [partial(count, num) for num in nums]
        call_coros = []

        for call in calls:
            call_coros.append(loop.run_in_executor(process_pool, call))

        results = await asyncio.gather(*call_coros)
        for result in results:
            print(result)

if __name__ == "__main__":
    asyncio.run(main())
