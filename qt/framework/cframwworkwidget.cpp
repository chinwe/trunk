#include "cframwworkwidget.h"
#include "ui_cframwworkwidget.h"
#include <testcomponent.h>

CFramwWorkWidget::CFramwWorkWidget(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::CFramwWorkWidget)
{
    ui->setupUi(this);

    Testcomponent tp;
    setWindowTitle(tp.name());
}

CFramwWorkWidget::~CFramwWorkWidget()
{
    delete ui;
}
