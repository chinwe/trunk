#include "stdafx.h"
#include "StateMachine.h"
#include <QtWidgets/QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    StateMachine w;
    w.show();
    return a.exec();
}
