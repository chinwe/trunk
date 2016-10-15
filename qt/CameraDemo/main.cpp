#include "camerademo.h"
#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    CameraDemo w;
    w.show();

    return a.exec();
}
