#include "cframwworkwidget.h"
#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    CFramwWorkWidget w;
    w.show();

    return a.exec();
}
