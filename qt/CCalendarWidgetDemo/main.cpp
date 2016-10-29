#include "CalendarWidgetDemo.h"
#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    CCalendarWidgetDemo w;
    w.show();

    return a.exec();
}
