#include "widget.h"
#include "ui_widget.h"

Widget::Widget(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Widget)
{
    ui->setupUi(this);

    ui->m_pListWidget->setCurrentRow(4);

    connect(ui->m_pListWidget, SIGNAL(currentItemChanged(QListWidgetItem*,QListWidgetItem*)),
            this, SLOT(OnCurrentItemChanged(QListWidgetItem*,QListWidgetItem*)));
}

Widget::~Widget()
{
    delete ui;
}

void Widget::OnCurrentItemChanged(QListWidgetItem *current, QListWidgetItem *previous)
{
    disconnect(ui->m_pListWidget, SIGNAL(currentItemChanged(QListWidgetItem*,QListWidgetItem*)),
            this, SLOT(OnCurrentItemChanged(QListWidgetItem*,QListWidgetItem*)));

    ui->m_pListWidget->clearSelection();


    connect(ui->m_pListWidget, SIGNAL(currentItemChanged(QListWidgetItem*,QListWidgetItem*)),
            this, SLOT(OnCurrentItemChanged(QListWidgetItem*,QListWidgetItem*)));
}
