import typer


from users import app as users_app
from version import app as version_app

app = typer.Typer(no_args_is_help=True)

app.add_typer(version_app)
app.add_typer(users_app, name="users")

if __name__ == "__main__":
    app()