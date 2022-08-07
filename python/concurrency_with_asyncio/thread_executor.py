import asyncio
# import functools
import requests
# from concurrent.futures import ThreadPoolExecutor

from util import async_timed

def get_status_code(url: str) -> int:
    response = requests.get(url)
    return response.status_code

@async_timed()
async def main():
    loop = asyncio.get_running_loop()
    '''
    with ThreadPoolExecutor() as pool:
        urls = ['https://docs.aiohttp.org' for _ in range(10)]
        tasks = [loop.run_in_executor(pool, functools.partial(get_status_code, url)) for url in urls]
        results = await asyncio.gather(*tasks)
        print(results)
    '''
    urls = ['https://docs.aiohttp.org' for _ in range(10)]
    tasks = [asyncio.to_thread(get_status_code, url) for url in urls]
    results = await asyncio.gather(*tasks)
    print(results)

asyncio.run(main())