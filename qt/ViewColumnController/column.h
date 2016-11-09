#ifndef COLUMN_H
#define COLUMN_H

#include <QWidget>
#include <QTreeView>
#include <QCheckBox>
#include <QVector>

namespace Ui {
class Column;
}

class Column : public QWidget
{
    Q_OBJECT

public:
    explicit Column(QWidget *parent = 0);
    ~Column();

    void BindView(QTreeView* pView);

private slots:
    void OnStateChanged(int iState);

private:
    Ui::Column *ui;

    QTreeView* m_pTreeView;

    QVector<QCheckBox*> m_vecChkBox;
};

#endif // COLUMN_H
