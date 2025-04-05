from typing import Union
from fastapi import FastAPI

import time

app = FastAPI()

@app.get("/")
async def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
async def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}

@app.get("/sleep")
async def sleep():
    time.sleep(10)
    return {"message": "slept for 5 seconds"}
