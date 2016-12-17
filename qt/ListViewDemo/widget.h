#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>

class QListWidgetItem;

namespace Ui {
class Widget;
}

class Widget : public QWidget
{
    Q_OBJECT

public:
    explicit Widget(QWidget *parent = 0);
    ~Widget();


private slots:
    void OnCurrentItemChanged(QListWidgetItem *current, QListWidgetItem *previous);

private:
    Ui::Widget *ui;
};

#endif // WIDGET_H
