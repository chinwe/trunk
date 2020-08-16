#pragma once

#include <QtWidgets/QWidget>
#include "ui_StateMachine.h"

class StateMachine : public QWidget
{
    Q_OBJECT

public:
    StateMachine(QWidget *parent = Q_NULLPTR);

private:
    Ui::StateMachineClass ui;
};
