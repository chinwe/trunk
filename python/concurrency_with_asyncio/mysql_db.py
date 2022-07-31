import asyncio
import aiomysql

PASSWORD = ''

async def test_pool():
    pool = await aiomysql.create_pool(host='127.0.0.1',
                                      port=3306,
                                      user='root',
                                      password=PASSWORD,
                                      db='dev')
    async with pool.acquire() as conn:
        async with conn.cursor() as cur:
            await cur.execute("SELECT 42;")
            print(cur.description)
            (r,) = await cur.fetchone()
            assert r == 42
    pool.close()
    await pool.wait_closed()
    
async def test_connect():
    async with aiomysql.connect(host='127.0.0.1',
                                port=3306,
                                user='root',
                                password=PASSWORD,
                                db='dev'
                                ) as conn:
        async with conn.cursor() as cur:
            await cur.execute("SELECT * FROM user")
            r = await cur.fetchall()
            print(r)

asyncio.run(test_connect())

