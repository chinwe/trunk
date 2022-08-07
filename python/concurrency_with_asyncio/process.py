import asyncio
from asyncio.subprocess import Process

async def main():
    process: Process = await asyncio.create_subprocess_exec("echo", "asyncio")
    status_code = await process.wait()
    print(f'Status code: {status_code}')

asyncio.run(main())