
import asyncio
from aiohttp import ClientSession
import aiohttp
from util import async_timed

@async_timed()
async def fetch_status(session: ClientSession, url: str) -> int:
    timeout_seconds = aiohttp.ClientTimeout(total=10)
    async with session.get(url, timeout=timeout_seconds) as response:
        return response.status

@async_timed()
async def main():
    async with aiohttp.ClientSession() as session:
        url = 'https://docs.aiohttp.org'
        status = await fetch_status(session, url)
        print(f'Status for {url} was {status}')

asyncio.run(main())
