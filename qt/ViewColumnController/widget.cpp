#include "widget.h"
#include "ui_widget.h"
#include "mymodel.h"

#include <QDebug>

Widget::Widget(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Widget)
{
    ui->setupUi(this);

    MyModel* pModel = new MyModel(this);
    ui->m_pTreeView->setModel(pModel);

    m_pCol = new Column();

    connect(ui->m_pBtnColumnShowOrHide, SIGNAL(clicked()), this, SLOT(OnColBtnClikced()));
}

Widget::~Widget()
{
    delete ui;
}

void Widget::OnColBtnClikced()
{
    m_pCol->BindView(ui->m_pTreeView);
    QPoint pt = ui->m_pBtnColumnShowOrHide->geometry().bottomLeft();

    m_pCol->move(mapToGlobal(pt));
    m_pCol->show();

}
