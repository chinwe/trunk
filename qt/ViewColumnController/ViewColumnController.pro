#-------------------------------------------------
#
# Project created by QtCreator 2016-11-09T23:02:37
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = ViewColumnController
TEMPLATE = app


SOURCES += main.cpp\
        widget.cpp \
    mymodel.cpp \
    column.cpp

HEADERS  += widget.h \
    mymodel.h \
    column.h

FORMS    += widget.ui \
    column.ui
