#ifndef CFRAMWWORKWIDGET_H
#define CFRAMWWORKWIDGET_H

#include <QWidget>

namespace Ui {
class CFramwWorkWidget;
}

class CFramwWorkWidget : public QWidget
{
    Q_OBJECT

public:
    explicit CFramwWorkWidget(QWidget *parent = 0);
    ~CFramwWorkWidget();

private:
    Ui::CFramwWorkWidget *ui;
};

#endif // CFRAMWWORKWIDGET_H
