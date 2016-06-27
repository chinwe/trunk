#include "widget.h"
#include "ui_widget.h"
#include <QDebug>

Widget::Widget(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Widget)
{
    ui->setupUi(this);

    ui->m_pEdtPassword->setEchoMode(QLineEdit::Password);

    QString strInputMask = ui->m_pEdtPassword->inputMask();

    Qt::InputMethodHints inputMethodHints = ui->m_pEdtPassword->inputMethodHints();

    qDebug() << inputMethodHints;

    inputMethodHints = ui->m_pEdtUserName->inputMethodHints();

    qDebug() << inputMethodHints;

    ui->m_pEdtPassword->setInputMethodHints(inputMethodHints);

    inputMethodHints = ui->m_pEdtPassword->inputMethodHints();

    qDebug() << inputMethodHints;
}

Widget::~Widget()
{
    delete ui;
}
