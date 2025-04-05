import typer

app = typer.Typer()

__version__ = "0.1.0"

@app.command()
def version():
    print(f"Awesome CLI version {__version__}")
