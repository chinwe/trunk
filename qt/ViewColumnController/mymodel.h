#ifndef MYMODEL_H
#define MYMODEL_H

#include <QStandardItemModel>

class MyModel : public QStandardItemModel
{
    Q_OBJECT

public:
    explicit MyModel(QObject* pParent = 0);

private:
};

#endif // MYMODEL_H
