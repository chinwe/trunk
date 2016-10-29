#ifndef CALENDARWIDGETDEMO_H
#define CALENDARWIDGETDEMO_H

#include <QWidget>

namespace Ui {
class CCalendarWidgetDemo;
}

class CCalendarWidgetDemo : public QWidget
{
    Q_OBJECT

public:
    explicit CCalendarWidgetDemo(QWidget *parent = 0);
    ~CCalendarWidgetDemo();

private slots:
    void OnClick(const QDate &date);

private:
    Ui::CCalendarWidgetDemo *ui;
};

#endif // CALENDARWIDGETDEMO_H
